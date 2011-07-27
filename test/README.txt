JUnit Test Case Structure
February 8th, 2010
---

Unit Tests

    Unit tests are simple tests that (usually) place a single class under test, exercising
    small units of work with each test case. Unit tests should test classes as POJO's, not
    as Spring components or classes that require tight coupling.

    Unit tests do not require the container or a prepared database to run.

        * ant test-unit


Web Service Tests

    Web service tests are designed to test the exposed jBilling remoting beans and the remote
    Hessian API. These tests are a proof-of-concept for the core of jBilling, and provide
    reference for developers looking to use the API in their own code.

    Web service tests require a running container with jBilling deployed, and a prepared database.

        * ant test-ws


Integration Tests

    Integration tests are designed to test the system as a whole. These tests trigger a long running
    process that touches on multiple area of the system, and test the outcome.

    Integration tests require a running container with jBilling deployed, and a prepared database.

        * ant test-integration

        or, execute single test classes individually

        * ant test-mediation
        * ant test-partners
        * ant test-provisioning
        * ant test-process


** You must run 'ant prepare-test' or 'ant dclean' prior to running either the web service or integration tests.