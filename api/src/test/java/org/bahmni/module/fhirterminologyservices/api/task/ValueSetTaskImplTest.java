package org.bahmni.module.fhirterminologyservices.api.task;

import ca.uhn.fhir.context.FhirContext;
import org.bahmni.module.fhirterminologyservices.api.TSConceptService;
import org.bahmni.module.fhirterminologyservices.api.TerminologyLookupService;
import org.bahmni.module.fhirterminologyservices.api.task.impl.ValueSetTaskImpl;
import org.bahmni.module.fhirterminologyservices.utils.TerminologyServicesException;
import org.hl7.fhir.r4.model.ValueSet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.fhir2.api.dao.FhirTaskDao;
import org.openmrs.module.fhir2.model.FhirTask;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.beans.factory.annotation.Qualifier;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
@PowerMockIgnore("javax.management.*")
public class ValueSetTaskImplTest {

    @Mock
    private TerminologyLookupService terminologyLookupService;

    @Mock
    private TSConceptService tsConceptService;

    @Mock
    private FhirTaskDao fhirTaskDao;

    @InjectMocks
    private ValueSetTaskImpl valueSetTask;

    @Mock
    @Qualifier("adminService")
    AdministrationService administrationService;

    @Before
    public void setUp() {
        PowerMockito.mockStatic(Context.class);
        Locale defaultLocale = new Locale("en", "GB");
        when(Context.getLocale()).thenReturn(defaultLocale);
        when(Context.getAdministrationService()).thenReturn(administrationService);
    }

    @Test
    public void shouldCreateFhirTask_whenRequestedWithListOfValueSetIds() {
        List<String> valueSetIds = Arrays.asList("procedure-set-1", "procedure-set-2", "procedure-set-3");
        when(fhirTaskDao.createOrUpdate(any(FhirTask.class))).thenReturn(null);

        FhirTask initialTaskResponse = valueSetTask.getInitialTaskResponse(valueSetIds);

        Assert.assertNotNull(initialTaskResponse);
        Assert.assertEquals(FhirTask.TaskStatus.ACCEPTED, initialTaskResponse.getStatus());
        Assert.assertEquals("Create / Update Value Set", initialTaskResponse.getName());
    }

    @Test
    public void shouldCreateConcepts_whenListOfValueSetIdsProvidedAndMetaInfoProvided() throws Exception {
        List<String> valueSetIds = Collections.singletonList("procedure-set-1");
        FhirTask initialTaskResponse = valueSetTask.getInitialTaskResponse(valueSetIds);
        ValueSet mockValueSet = getMockValueSet();
        when(administrationService.getGlobalProperty(RestConstants.MAX_RESULTS_DEFAULT_GLOBAL_PROPERTY_NAME)).thenReturn("50");
        when(terminologyLookupService.getValueSetByPageSize("procedure-set-1", "en", 50, 0)).thenReturn(mockValueSet);

        valueSetTask.convertValueSetsToConceptsTask(valueSetIds, "en", "Procedure", "N/A", "Procedure Orders", initialTaskResponse, Context.getUserContext());

        verify(fhirTaskDao, times(2)).createOrUpdate(initialTaskResponse);
        Assert.assertEquals(FhirTask.TaskStatus.COMPLETED, initialTaskResponse.getStatus());
    }

    @Test
    public void shouldFetchConceptsInPages_whenListOfValueSetIdsProvidedAndMetaInfoProvided() throws Exception {
        List<String> valueSetIds = Collections.singletonList("procedure-set-1");
        FhirTask initialTaskResponse = valueSetTask.getInitialTaskResponse(valueSetIds);
        ValueSet mockValueSet = getMockValueSet();
        when(administrationService.getGlobalProperty(RestConstants.MAX_RESULTS_DEFAULT_GLOBAL_PROPERTY_NAME)).thenReturn("2");
        when(terminologyLookupService.getValueSetByPageSize(anyString(), anyString(), anyInt(), anyInt())).thenReturn(mockValueSet);

        valueSetTask.convertValueSetsToConceptsTask(valueSetIds, "en", "Procedure", "N/A", "Procedure Orders", initialTaskResponse, Context.getUserContext());

        verify(fhirTaskDao, times(2)).createOrUpdate(initialTaskResponse);
        verify(terminologyLookupService, times(5)).getValueSetByPageSize(anyString(), anyString(), anyInt(), anyInt());
        Assert.assertEquals(FhirTask.TaskStatus.COMPLETED, initialTaskResponse.getStatus());
    }

    @Test
    public void shouldThrowException_whenValueSetIsNotFoundInTheTerminologyServer() {
        List<String> valueSetIds = Collections.singletonList("random-valueset-uuid-1");
        FhirTask initialTaskResponse = valueSetTask.getInitialTaskResponse(valueSetIds);
        when(administrationService.getGlobalProperty(RestConstants.MAX_RESULTS_DEFAULT_GLOBAL_PROPERTY_NAME)).thenReturn("50");
        when(terminologyLookupService.getValueSetByPageSize("random-valueset-uuid-1", "en", 50, 0)).thenThrow(TerminologyServicesException.class);

        valueSetTask.convertValueSetsToConceptsTask(valueSetIds, "en", "Procedure", "N/A", "Procedure Orders", initialTaskResponse, Context.getUserContext());

        verify(fhirTaskDao, times(2)).createOrUpdate(initialTaskResponse);
        Assert.assertEquals(FhirTask.TaskStatus.REJECTED, initialTaskResponse.getStatus());
    }

    @Test
    public void shouldRemoveBodySiteConceptFromProcedureOrdersSetMembers_whenValueSetIsNotFoundInTheTerminologyServer() {
        List<String> valueSetIds = Collections.singletonList("random-valueset-uuid-1");
        FhirTask initialTaskResponse = valueSetTask.getInitialTaskResponse(valueSetIds);
        when(administrationService.getGlobalProperty(RestConstants.MAX_RESULTS_DEFAULT_GLOBAL_PROPERTY_NAME)).thenReturn("50");
        ValueSet emptyValueSet = getEmptyValueSet();
        when(terminologyLookupService.getValueSetByPageSize("random-valueset-uuid-1", "en", 50, 0)).thenReturn(emptyValueSet);
        valueSetTask.convertValueSetsToConceptsTask(valueSetIds, "en", "Procedure", "N/A", "Procedure Orders", initialTaskResponse, Context.getUserContext());

        verify(tsConceptService, times(1)).removeMemberFromConceptSet(anyString(), anyString());
        verify(fhirTaskDao, times(2)).createOrUpdate(initialTaskResponse);
        Assert.assertEquals(FhirTask.TaskStatus.COMPLETED, initialTaskResponse.getStatus());
    }


    private ValueSet getMockValueSet() throws Exception {
        Path path = Paths.get(getClass().getClassLoader()
                .getResource("mock/TsMockResponseForProceduresValueSet.json").toURI());
        String mockString = Files.lines(path, StandardCharsets.UTF_8).collect(Collectors.joining("\n"));
        return FhirContext.forR4().newJsonParser().parseResource(ValueSet.class, mockString);
    }

    private ValueSet getEmptyValueSet() {
        ValueSet valueSet = new ValueSet();
        valueSet.setExpansion(new ValueSet.ValueSetExpansionComponent());
        valueSet.getExpansion().setTotal(0);
        return valueSet;
    }

}
