/*
 jBilling - The Enterprise Open Source Billing System
 Copyright (C) 2003-2011 Enterprise jBilling Software Ltd. and Emiliano Conde

 This file is part of jbilling.

 jbilling is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 jbilling is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with jbilling.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.sapienter.jbilling.server.mediation;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.EntityNotFoundException;

import com.sapienter.jbilling.server.mediation.db.MediationRecordStatusDAS;
import com.sapienter.jbilling.server.mediation.db.MediationRecordStatusDTO;
import com.sapienter.jbilling.server.mediation.task.IMediationErrorHandler;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskException;
import org.apache.log4j.Logger;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.sapienter.jbilling.common.InvalidArgumentException;
import com.sapienter.jbilling.common.SessionInternalError;
import com.sapienter.jbilling.server.mediation.db.MediationConfiguration;
import com.sapienter.jbilling.server.mediation.db.MediationConfigurationDAS;
import com.sapienter.jbilling.server.mediation.db.MediationMapDAS;
import com.sapienter.jbilling.server.mediation.db.MediationOrderMap;
import com.sapienter.jbilling.server.mediation.db.MediationProcess;
import com.sapienter.jbilling.server.mediation.db.MediationProcessDAS;
import com.sapienter.jbilling.server.mediation.db.MediationRecordDAS;
import com.sapienter.jbilling.server.mediation.db.MediationRecordDTO;
import com.sapienter.jbilling.server.mediation.db.MediationRecordLineDAS;
import com.sapienter.jbilling.server.mediation.db.MediationRecordLineDTO;
import com.sapienter.jbilling.server.mediation.task.IMediationProcess;
import com.sapienter.jbilling.server.mediation.task.IMediationReader;
import com.sapienter.jbilling.server.mediation.task.MediationResult;
import com.sapienter.jbilling.server.order.db.OrderLineDAS;
import com.sapienter.jbilling.server.order.db.OrderLineDTO;
import com.sapienter.jbilling.server.pluggableTask.TaskException;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskBL;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskDAS;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskDTO;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskManager;
import com.sapienter.jbilling.server.user.EntityBL;
import com.sapienter.jbilling.server.util.Constants;
import com.sapienter.jbilling.server.util.Context;
import com.sapienter.jbilling.server.util.audit.EventLogger;
import java.util.ArrayList;
import java.util.Map;

import org.springframework.util.StopWatch;

/**
 *
 * @author emilc
 **/
@Transactional( propagation = Propagation.REQUIRED )
public class MediationSessionBean implements IMediationSessionBean {
    private static final Logger LOG = Logger.getLogger(MediationSessionBean.class);

    private static StopWatch stopWatch = null;

    /**
     * Trigger the mediation process. Only one mediation process can be running at any given
     * time, this method will not start an additional mediation process if one is already running.
     */
    public void trigger(Integer entityId) {
        LOG.debug("Running mediation trigger for entity " + entityId);
        StopWatch watch = new StopWatch("trigger watch");
        watch.start();

        // local instance of this bean to invoke transactional methods
        IMediationSessionBean local = (IMediationSessionBean) Context.getBean(Context.Name.MEDIATION_SESSION);

        /*
            There can only be one process running for this entity, check that there is
            no other mediation process running for this entity before continuing.
         */
        if (local.isProcessing(entityId)) {
            LOG.debug("Entity " + entityId + " already has a running mediation process, skipping run");
            return;
        }

        // fetch mediation processing plug in (usually a rules based processor)
        IMediationProcess processTask;
        try {
            PluggableTaskManager<IMediationProcess> taskManager
                    = new PluggableTaskManager<IMediationProcess>(entityId, Constants.PLUGGABLE_TASK_MEDIATION_PROCESS);
            processTask = taskManager.getNextClass();
        } catch (PluggableTaskException e) {
            throw new SessionInternalError("Could not retrieve mediation process plug-in.", e);
        }

        if (processTask == null) {
            LOG.debug("Entity " + entityId + " does not have a mediation process plug-in");
            return;
        }

        // find the root user of this entity, will be used as the executor for order updates
        Integer executorId = new EntityBL().getRootUser(entityId);
        List<String> errorMessages = new ArrayList<String>();

        // process each mediation configuration for this entity
        for (MediationConfiguration cfg : local.getAllConfigurations(entityId)) {
            /*
                Double check that we're still the only mediation process running before we create
                a new MediationProcess record. The mediation processing plug-in may have a long
                instantiation time which leaves a window for another overlapping process to be created.
             */
            if (local.isProcessing(entityId)) {
                LOG.debug("Entity " + entityId + " already has an existing mediation process, skipping run");
                return;
            }

            // create process record and mark start time
            LOG.debug("Now using configuration " + cfg);
            MediationProcess process = local.createProcessRecord(cfg);

            // fetch mediation reader plug-in
            IMediationReader reader;
            try {
                PluggableTaskBL<IMediationReader> readerTask = new PluggableTaskBL<IMediationReader>();
                readerTask.set(cfg.getPluggableTask());
                reader = readerTask.instantiateTask();
            } catch (PluggableTaskException e) {
                throw new SessionInternalError("Could not instantiate mediation reader plug-in.", e);
            }

            // read records and normalize using the MediationProcess plug-in
            if (reader.validate(errorMessages)) {
                /*
                    Catch exceptions and log errors instead of re-throwing as SessionInternalError
                    so that the remaining mediation configurations can be run, and so that this
                    process can be "completed" by setting the end date.
                 */
                try {
                    stopWatch = new StopWatch();
                    stopWatch.start("Reading records");
                    for (List<Record> thisGroup : reader) {
                        stopWatch.stop();
                        LOG.debug("Now processing " + thisGroup.size() + " records.");
                        local.normalizeRecordGroup(processTask, executorId, process, thisGroup, entityId, cfg);
                        LOG.debug(stopWatch.prettyPrint());
                        stopWatch = new StopWatch();
                        stopWatch.start("Reading records");
                    }
                } catch (TaskException e) {
                    LOG.error("Exception occurred processing mediation records.", e);
                } catch (Throwable t) {
                    LOG.error("Unhandled exception occurred during mediation.", t);
                }
            }

            // mark process end date
            local.updateProcessRecord(process, new Date());
            LOG.debug("Configuration '" + cfg.getName() + "' finished at " + process.getEndDatetime());
        }

        // throw a SessionInternalError of errors were returned from the reader plug-in
        if (!errorMessages.isEmpty()) {
            StringBuffer buffer = new StringBuffer("Invalid reader plug-in configuration \n");
            for (String message : errorMessages) {
                buffer.append("ERROR: ")
                    .append(message)
                    .append("\n");
            }
            throw new SessionInternalError(buffer.toString());
        }

        watch.stop();
        LOG.debug("Mediation process done. Duration (ms):" + watch.getTotalTimeMillis());
    }


    /**
     * Create a new MediationProcess for the given configuration, marking the
     * start time of the process and initializing the affected order count.
     *
     * @param cfg mediation configuration
     * @return new MediationProcess record
     */
    @Transactional( propagation = Propagation.REQUIRES_NEW )
    public MediationProcess createProcessRecord(MediationConfiguration cfg) {
        MediationProcessDAS processDAS = new MediationProcessDAS();
        MediationProcess process = new MediationProcess();
        process.setConfiguration(cfg);
        process.setStartDatetime(Calendar.getInstance().getTime());
        process.setOrdersAffected(0);
        process = processDAS.save(process);
        processDAS.flush();

        return process;
    }

    /**
     * Updated the end time of the given MediationProcess, effectively marking
     * the process as completed.
     *
     * @param process MediationProcess to update
     * @param enddate end time to set
     * @return updated MediationProcess record
     */
    @Transactional( propagation = Propagation.REQUIRES_NEW )
    public MediationProcess updateProcessRecord(MediationProcess process, Date enddate) {
        new MediationProcessDAS().reattach(process);
        process.setEndDatetime(enddate);
        return process;
    }

    /**
     * Returns true if a running MediationProcess exists for the given entity id. A
     * process is considered to be running if it does not have an end time.
     *
     * @param entityId entity id to check
     * @return true if a process is running for the given entity, false if not
     */
    @Transactional( propagation = Propagation.REQUIRES_NEW )
    public boolean isProcessing(Integer entityId) {
        return new MediationProcessDAS().isProcessing(entityId);
    }

    public List<MediationProcess> getAll(Integer entityId) {
        MediationProcessDAS processDAS = new MediationProcessDAS();
        List<MediationProcess> result = processDAS.findAllByEntity(entityId);
        processDAS.touch(result);
        return result;

    }

    /**
     * Returns a list of all MediationConfiguration's for the given entity id.
     *
     * @param entityId entity id
     * @return list of mediation configurations for entity, empty list if none found
     */
    public List<MediationConfiguration> getAllConfigurations(Integer entityId) {
        MediationConfigurationDAS cfgDAS = new MediationConfigurationDAS();
        return cfgDAS.findAllByEntity(entityId);
    }

    public void createConfiguration(MediationConfiguration cfg) {
        MediationConfigurationDAS cfgDAS = new MediationConfigurationDAS();

        cfg.setCreateDatetime(Calendar.getInstance().getTime());
        cfgDAS.save(cfg);

    }

    public List<MediationConfiguration> updateAllConfiguration(Integer executorId, List<MediationConfiguration> configurations)
            throws InvalidArgumentException {
        MediationConfigurationDAS cfgDAS = new MediationConfigurationDAS();
        List<MediationConfiguration> retValue = new ArrayList<MediationConfiguration>();
        try {

            for (MediationConfiguration cfg : configurations) {
                // if the configuration is new, the task needs to be loaded
                if (cfg.getPluggableTask().getEntityId() == null) {
                    PluggableTaskDAS pt = (PluggableTaskDAS) Context.getBean(Context.Name.PLUGGABLE_TASK_DAS);
                    PluggableTaskDTO task = pt.find(cfg.getPluggableTask().getId());
                    if (task != null && task.getEntityId().equals(cfg.getEntityId())) {
                        cfg.setPluggableTask(task);
                    } else {
                        throw new InvalidArgumentException("Task not found or " +
                                "entity of pluggable task is not the same when " +
                                "creating a new mediation configuration", 1);
                    }
                }
                retValue.add(cfgDAS.save(cfg));
            }
            return retValue;
        } catch (EntityNotFoundException e1) {
            throw new InvalidArgumentException("Wrong data saving mediation configuration", 1, e1);
        } catch (InvalidArgumentException e2) {
            throw new InvalidArgumentException(e2);
        } catch (Exception e) {
            throw new SessionInternalError("Exception updating mediation configurations ", MediationSessionBean.class, e);
        }
    }

    public void delete(Integer executorId, Integer cfgId) {
        MediationConfigurationDAS cfgDAS = new MediationConfigurationDAS();

        cfgDAS.delete(cfgDAS.find(cfgId));
        EventLogger.getInstance().audit(executorId, null,
                                        Constants.TABLE_MEDIATION_CFG, cfgId,
                                        EventLogger.MODULE_MEDIATION, EventLogger.ROW_DELETED, null,
                                        null, null);
    }

    /**
     * Calculation number of records for each of the existing mediation record statuses
     *
     * @param entityId EntityId for searching mediationRecords
     * @return map of mediation status as a key and long value as a number of records whit given status
     */
    public Map<MediationRecordStatusDTO, Long> getNumberOfRecordsByStatuses(Integer entityId) {
        MediationRecordDAS recordDas = new MediationRecordDAS();
        MediationRecordStatusDAS recordStatusDas = new MediationRecordStatusDAS();
        Map<MediationRecordStatusDTO, Long> resultMap = new HashMap<MediationRecordStatusDTO, Long>();
        List<MediationRecordStatusDTO> statuses = recordStatusDas.findAll();

        //propagate proxy objects for using out of the transaction
        recordStatusDas.touch(statuses);
        for (MediationRecordStatusDTO status : statuses) {
            Long recordsCount = recordDas.countMediationRecordsByEntityIdAndStatus(entityId, status);
            resultMap.put(status, recordsCount);
        }
        return resultMap;
    }

    public boolean hasBeenProcessed(MediationProcess process, Record record) {
        MediationRecordDAS recordDas = new MediationRecordDAS();

        // validate that this group has not been already processed
        if (recordDas.processed(record.getKey())) {
            LOG.debug("Detected duplicated of record: " + record.getKey());
            return true;
        }
        LOG.debug("Detected record as a new event: " + record.getKey());

        // assign to record DONE_AND_BILLABLE status as default before processing
        // after actual processing it will be updated
        MediationRecordStatusDTO status = new MediationRecordStatusDAS().find(Constants.MEDIATION_RECORD_STATUS_DONE_AND_BILLABLE);
        MediationRecordDTO dbRecord = new MediationRecordDTO(record.getKey(),
                                                             Calendar.getInstance().getTime(),
                                                             process,
                                                             status);
        recordDas.save(dbRecord);
        recordDas.flush();

        return false;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void normalizeRecordGroup(IMediationProcess processTask, Integer executorId,
                                     MediationProcess process, List<Record> thisGroup, Integer entityId,
                                     MediationConfiguration cfg) throws TaskException {

        stopWatch.start("Pre-processing");

        LOG.debug("Normalizing " + thisGroup.size() + " records ...");

        // this process came from a different transaction (persistent context)
        new MediationProcessDAS().reattachUnmodified(process);

        // validate that these records have not been already processed
        for (Iterator<Record> it = thisGroup.iterator(); it.hasNext();) {
            if (hasBeenProcessed(process, it.next())) it.remove();
        }

        if (thisGroup.size() == 0) {
            return; // it could be that they all have been processed already
        }

        ArrayList<MediationResult> results = new ArrayList<MediationResult>(0);

        // call the plug-in to resolve these records
        stopWatch.stop();
        stopWatch.start("Processing");
        processTask.process(thisGroup, results, cfg.getName());
        stopWatch.stop();
        stopWatch.start("Post-Processing");

        LOG.debug("Processing " + thisGroup.size()
                + " records took: " + stopWatch.getLastTaskTimeMillis() + "ms,"
                + " or " + new Double(thisGroup.size()) / stopWatch.getLastTaskTimeMillis() * 1000D + " records/sec");

        // go over the results
        for (MediationResult result : results) {
            if (!result.isDone()) {
                // this is an error, the rules failed somewhere because the
                // 'done' flag is still false.
                LOG.debug("Record result is not done");

                // errors presented, status of record should be updated
                assignStatusToMediationRecord(result.getRecordKey(),
                                              new MediationRecordStatusDAS().find(Constants.MEDIATION_RECORD_STATUS_ERROR_DETECTED));

                // call error handler for mediation errors
                handleMediationErrors(findRecordByKey(thisGroup, result.getRecordKey()),
                                      resolveMediationResultErrors(result),
                                      entityId, cfg);

            } else if (!result.getErrors().isEmpty()) {
                // There are some user-detected errors
                LOG.debug("Record result is done with errors");

                //done, but errors assigned by rules. status of record should be updated
                assignStatusToMediationRecord(result.getRecordKey(),
                                              new MediationRecordStatusDAS().find(Constants.MEDIATION_RECORD_STATUS_ERROR_DECLARED));
                // call error handler for rules errors
                handleMediationErrors(findRecordByKey(thisGroup, result.getRecordKey()),
                                      result.getErrors(),
                                      entityId, cfg);
            } else {
                // this record was process without any errors
                LOG.debug("Record result is done");

                if (result.getLines() == null || result.getLines().isEmpty()) {
                    //record was processed, but order lines was not affected
                    //now record has status DONE_AND_BILLABLE, it should be changed
                    assignStatusToMediationRecord(result.getRecordKey(),
                                                  new MediationRecordStatusDAS().find(Constants.MEDIATION_RECORD_STATUS_DONE_AND_NOT_BILLABLE));
                    //not needed to update order affected or lines in this case

                } else {
                    //record has status DONE_AND_BILLABLE, only needed to save processed lines
                    process.setOrdersAffected(process.getOrdersAffected() + result.getLines().size());

                    // relate this order with this process
                    MediationOrderMap map = new MediationOrderMap();
                    map.setMediationProcessId(process.getId());
                    map.setOrderId(result.getCurrentOrder().getId());

                    MediationMapDAS mapDas = new MediationMapDAS();
                    mapDas.save(map);

                    // add the record lines
                    // todo: could be problematic if asynchronous mediation processes are running.
                    // a better approach is to link MediationResult to the record by the unique ID -- future enhancement
                    saveEventRecordLines(result.getDiffLines(), new MediationRecordDAS().findNewestByKey(result.getRecordKey()),
                                         result.getEventDate(),
                                         result.getDescription());
                }
            }
        }

        stopWatch.stop();
    }

    public void saveEventRecordLines(List<OrderLineDTO> newLines, MediationRecordDTO record, Date eventDate,
                                     String description) {

        MediationRecordLineDAS mediationRecordLineDas = new MediationRecordLineDAS();

        for (OrderLineDTO line : newLines) {
            MediationRecordLineDTO recordLine = new MediationRecordLineDTO();

            recordLine.setEventDate(eventDate);
            OrderLineDTO dbLine = new OrderLineDAS().find(line.getId());
            recordLine.setOrderLine(dbLine);
            recordLine.setAmount(line.getAmount());
            recordLine.setQuantity(line.getQuantity());
            recordLine.setRecord(record);
            recordLine.setDescription(description);

            recordLine = mediationRecordLineDas.save(recordLine);
            // no need to link to the parent record. The association is completed already
            // record.getLines().add(recordLine);
        }
    }

    public List<MediationRecordLineDTO> getEventsForOrder(Integer orderId) {
        List<MediationRecordLineDTO> events = new MediationRecordLineDAS().getByOrder(orderId);
        for (MediationRecordLineDTO line : events) {
            line.toString(); //as a touch
        }
        return events;
    }

    public List<MediationRecordDTO> getMediationRecordsByMediationProcess(Integer mediationProcessId) {
        return new MediationRecordDAS().findByProcess(mediationProcessId);
    }

    private void assignStatusToMediationRecord(String key, MediationRecordStatusDTO status) {
        MediationRecordDAS recordDas = new MediationRecordDAS();
        MediationRecordDTO recordDto = recordDas.findNewestByKey(key);
        if (recordDto != null) {
            recordDto.setRecordStatus(status);
            recordDas.save(recordDto);
        } else {
            LOG.debug("Mediation record with key=" + key + " not found");
        }
    }

    private List<String> resolveMediationResultErrors(MediationResult result) {
        List<String> errors = new LinkedList<String>();
        if (result.getLines() == null || result.getLines().isEmpty()) {
            errors.add("JB-NO_LINE");
        }
        if (result.getDiffLines() == null || result.getDiffLines().isEmpty()) {
            errors.add("JB-NO_DIFF");
        }
        if (result.getCurrentOrder() == null) {
            errors.add("JB-NO_ORDER");
        }
        if (result.getUserId() == null) {
            errors.add("JB-NO_USER");
        }
        if (result.getCurrencyId() == null) {
            errors.add("JB-NO_CURRENCY");
        }
        if (result.getEventDate() == null) {
            errors.add("JB-NO_DATE");
        }
        errors.addAll(result.getErrors());
        return errors;
    }

    private Record findRecordByKey(List<Record> records, String key) {
        for (Record r : records) {
            if (r.getKey().equals(key)) {
                return r;
            }
        }
        return null;
    }

    private void handleMediationErrors(Record record,
                                       List<String> errors,
                                       Integer entityId,
                                       MediationConfiguration cfg) {
        if (record == null) return;
        StopWatch watch = new StopWatch("saving errors watch");
        watch.start();
        LOG.debug("Saving mediation result errors: " + errors.size());

        try {
            PluggableTaskManager<IMediationErrorHandler> tm = new PluggableTaskManager<IMediationErrorHandler>(entityId,
                    Constants.PLUGGABLE_TASK_MEDIATION_ERROR_HANDLER);
            IMediationErrorHandler errorHandler;
            // iterate through all error handlers for current entityId
            // and process errors
            while ((errorHandler = tm.getNextClass()) != null) {
                try {
                    errorHandler.process(record, errors, new Date(), cfg);
                } catch (TaskException e) {
                    // exception catched for opportunity of processing errors by other handlers
                    // and continue mediation process for other records
                    // TO-DO: check requirements about error handling in that case
                    LOG.error(e);
                }
            }

        } catch (PluggableTaskException e) {
            LOG.error(e);
            // it's possible plugin configuration exception
            // TO-DO: check requirements about error handling
            // may be rethrow exception
        }

        watch.stop();
        LOG.debug("Saving mediation result errors done. Duration (mls):" + watch.getTotalTimeMillis());
    }
}
