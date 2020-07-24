package org.openmrs.module.hivtestingservices.api.db;

import org.openmrs.module.hivtestingservices.model.MuzimaCohortMetadata;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface MuzimaCohortMetadataDao {
    @Transactional
    List<MuzimaCohortMetadata> saveOrUpdate(List<MuzimaCohortMetadata> object);

    @Transactional
    void delete(List<MuzimaCohortMetadata> object);

    List<Object> executeFilterQuery(String filterQuery);

    List<MuzimaCohortMetadata> getMuzimaCohortMetadata(List<Integer> patientIds, Integer cohortId);

}
