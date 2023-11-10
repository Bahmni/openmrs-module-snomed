# OpenMRS module SNOMED
This module provides integration with FHIR-compliant terminology server primarily for lookup of clinical terminologies

More details can be found in [this](https://bahmni.atlassian.net/wiki/spaces/BAH/pages/3132686337/SNOMED+FHIR+Terminology+Server+Integration+with+Bahmni) Wiki link

See API documentation [here](https://editor.swagger.io/?url=https://raw.githubusercontent.com/Bahmni/openmrs-module-snomed/main/omod/src/main/resources/openapi.yaml)

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

## Concept Resolution
When creating / updating concepts from Terminology Server, the module will attempt to resolve the concept to an existing concept in OpenMRS based on concept source (eg: SNOMED-CT) and Concept Code from terminology server. If a match is found, the concept will be updated with the new details from Terminology Server. If no match is found, a new concept will be created in OpenMRS in the user's locale.

### Scenario 1: OpenMRS concept does not exist
Concept Source: SNOMED-CT

Concept Code: 61462000 

Locale: en

Fully Specified Name in TS : Malaria (disorder)

Preferred Name in TS: Malaria 

Existing OpenMRS concept : None

In this scenario, we are creating a concept in OpenMRS with the following details 

Fully Specified Name = Malaria (disorder)

Short Name = Malaria

Locale = en

Concept Source = SNOMED-CT

Concept Code = 61462000 

Concept mappings < SAME AS >

### Scenario 2: OpenMRS concept exists (Fully Specified Name is available and Short Name is empty)
Concept Source: SNOMED-CT

Concept Code: 61462000

Locale: en

Fully Specified Name in TS : Malaria (disorder)

Preferred Name in TS: Malaria

Existing OpenMRS concept : 

Fully Specified Name = Malaria

Short Name = None

In this scenario, we are updating the concept in OpenMRS with the following details

Fully Specified Name = Malaria < No Change >

Synonym = Malaria (disorder) < Added >

Short Name = Malaria < Added >

Locale = en

Concept Source = SNOMED-CT

Concept Code = 61462000

Concept mappings < SAME AS >

### Scenario 3: OpenMRS concept exists (Fully Specified Name and Short Name is available)
Concept Source: SNOMED-CT

Concept Code: 61462000

Locale: en

Fully Specified Name in TS : Malaria (disorder)

Preferred Name in TS: Malaria

Existing OpenMRS concept :

Fully Specified Name = Malaria

Short Name = Malaria

In this scenario, we are updating the concept in OpenMRS with the following details

Fully Specified Name = Malaria < No Change >

Synonym = Malaria (disorder) < Added >

Short Name = Malaria < No Change >

Locale = en

Concept Source = SNOMED-CT

Concept Code = 61462000

Concept mappings < SAME AS >