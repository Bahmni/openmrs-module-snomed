package org.bahmni.module.fhirterminologyservices.api.impl;

import org.bahmni.module.fhirterminologyservices.api.mapper.ValueSetMapper;
import org.bahmni.module.fhirterminologyservices.utils.TerminologyServicesException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.Collections;
import java.util.List;


import static org.bahmni.module.fhirterminologyservices.api.impl.TerminologyLookupServiceImpl.TERMINOLOGY_SERVER_DOWN_ERROR_MESSAGE;
import static org.bahmni.module.fhirterminologyservices.api.mapper.impl.VSSimpleObjectMapper.CONCEPT_NAME;
import static org.bahmni.module.fhirterminologyservices.api.mapper.impl.VSSimpleObjectMapper.CONCEPT_UUID;
import static org.bahmni.module.fhirterminologyservices.api.mapper.impl.VSSimpleObjectMapper.MATCHED_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.powermock.api.mockito.PowerMockito.when;
@PowerMockIgnore("javax.management.*")
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Context.class })
public class TerminologyLookupServiceImplTest {
    @Mock
    private AdministrationService administrationService;
    @InjectMocks
    TerminologyLookupServiceImpl terminologyLookupService;
    @Mock
    private UserContext userContext;
   @Mock
   private ValueSetMapper<List<SimpleObject>> vsSimpleObjectMapper;
    @Before
    public void init() {
        PowerMockito.mockStatic(Context.class);
        when(Context.getAdministrationService()).thenReturn(administrationService);
    }


    @Test
    public void shouldReturnMatchingDiagnosisTermsFromTerminologyServer() throws IOException {

        when(vsSimpleObjectMapper.map(any())).thenReturn( createMockDiagnosisResponse());
        List<SimpleObject> diagnosisSearchList = terminologyLookupService.getResponseList("Malaria", 10, null);
        assertNotNull(diagnosisSearchList);
        assertEquals(1, diagnosisSearchList.size());
        SimpleObject firstResponse = diagnosisSearchList.get(0);
        assertEquals("Plasmodiosis", firstResponse.get(CONCEPT_NAME));
        assertEquals("61462000", firstResponse.get(CONCEPT_UUID));
        assertEquals("Plasmodiosis", firstResponse.get(MATCHED_NAME));
    }
    @Test
    public void shouldThrowErrorWhenTerminologyServerIsDown()  {

        when(vsSimpleObjectMapper.map(any())).thenReturn( createMockDiagnosisResponse());
        Exception exception = assertThrows(TerminologyServicesException.class, () -> {
            List<SimpleObject> diagnosisSearchList = terminologyLookupService.getResponseList("otherTerm", 10, null);
        });
        assertEquals(TERMINOLOGY_SERVER_DOWN_ERROR_MESSAGE, exception.getMessage());
    }
    private List<SimpleObject> createMockDiagnosisResponse() {
        SimpleObject diagnosisObject = new SimpleObject();
        diagnosisObject.add(CONCEPT_NAME, "Plasmodiosis");
        diagnosisObject.add(CONCEPT_UUID, "61462000");
        diagnosisObject.add(MATCHED_NAME,"Plasmodiosis");
        return Collections.singletonList(diagnosisObject) ;
    }
}