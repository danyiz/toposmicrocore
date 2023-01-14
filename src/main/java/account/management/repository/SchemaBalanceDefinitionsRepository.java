package account.management.repository;


import org.springframework.data.jpa.repository.JpaRepository;

public interface SchemaBalanceDefinitionsRepository extends JpaRepository<SchemaBalanceDefinitions,Long> {

    SchemaBalanceDefinitions findBySchemaCode(String schemaCode);
}

