package com.embabel.demo.domain;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CodeAnalysisRepository extends ListCrudRepository<StoredCodeAnalysis, String> {
}
