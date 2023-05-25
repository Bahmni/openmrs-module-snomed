# OpenMRS module SNOMED
This module provides integration with FHIR-compliant terminology server primarily for lookup of clinical terminologies

API Documentation [here](https://editor.swagger.io/?url=https://raw.githubusercontent.com/Bahmni/openmrs-module-snomed/main/omod/src/main/resources/openapi.yaml).

### Prerequisite
1. JDK 1.8
2. FHIR compatible Terminology Server (Reference server found [here](https://dev-is-browser.ihtsdotools.org/fhir/))


## Clone the repository and build the omod
```git clone https://github.com/bahmni/openmrs-module-snomed```

```cd openmrs-module-snomed```

```mvn clean package```

The output is the OMOD file:
```openmrs-module-snomed/omod/target/fhir-ts-services-int-[VERSION].omod```
## Deploy
Copy ```openmrs-module-snomed/omod/target/fhir-ts-services-int-[VERSION].omod``` into OpenMRS modules directory and restart OpenMRS