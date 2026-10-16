package com.aurum.main.repository;

import com.aurum.main.dto.SupplierDTO;
import com.aurum.main.dto.requests.SupplierQuery;
import com.aurum.main.model.Product.ProductCategory;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SupplierRepositoryCustomImplTests {
    private final JdbcClient jdbc = mock(JdbcClient.class);
    private final JdbcClient.StatementSpec statement = mock(JdbcClient.StatementSpec.class);
    private final SupplierRepositoryCustomImpl repository = new SupplierRepositoryCustomImpl(jdbc);

    @SuppressWarnings("unchecked")
    private void prepareQueries() {
        JdbcClient.MappedQuerySpec<Long> count = mock(JdbcClient.MappedQuerySpec.class);
        JdbcClient.MappedQuerySpec<SupplierDTO> rows = mock(JdbcClient.MappedQuerySpec.class);
        when(jdbc.sql(anyString())).thenReturn(statement);
        when(statement.params(anyMap())).thenReturn(statement);
        when(statement.query(Long.class)).thenReturn(count);
        when(statement.query(any(RowMapper.class))).thenReturn(rows);
        when(count.single()).thenReturn(21L);
        when(rows.list()).thenReturn(List.of(new SupplierDTO(
                "supplier-id", "Supplier", Set.of(ProductCategory.TABLEWARE), "City", "Street", "12")));
    }

    @Test
    void combinesAddressSearchWithCategoryAndReturnsPage() {
        prepareQueries();
        SupplierQuery query = new SupplierQuery();
        query.setSearch("CITY");
        query.setProductType(ProductCategory.TABLEWARE);
        query.setPage(2);
        query.setSize(5);
        query.setSortBy("name");
        query.setSortDirection("desc");

        var result = repository.search(query);

        verify(jdbc, times(2)).sql(argThat(sql -> sql.contains(
                "AND (LOWER(s.name) LIKE :search OR LOWER(s.city) LIKE :search " +
                        "OR LOWER(s.street) LIKE :search OR LOWER(s.number) LIKE :search) " +
                        " AND EXISTS (SELECT 1 FROM supplier_categories sc " +
                        "WHERE sc.supplier_id = s.id AND sc.category = :productType)")));
        verify(jdbc).sql(contains("ORDER BY s.name DESC, s.id ASC LIMIT :limit OFFSET :offset"));
        verify(statement, times(2)).params(Map.of(
                "search", "%city%", "productType", "TABLEWARE", "limit", 5, "offset", 10L));
        assertEquals(21L, result.totalElements());
        assertEquals(2, result.pageNumber());
        assertEquals(5, result.pageSize());
        assertEquals(Set.of(ProductCategory.TABLEWARE), result.content().getFirst().categories());
    }

    @Test
    void ignoresBlankSearchAndUsesSafeSortAndPaginationDefaults() {
        prepareQueries();
        SupplierQuery query = new SupplierQuery();
        query.setSearch("  ");
        query.setPage(-1);
        query.setSize(0);
        query.setSortBy("name; DROP TABLE supplier");
        query.setSortDirection("DESC; DROP TABLE supplier");

        var result = repository.search(query);

        verify(jdbc).sql("SELECT COUNT(*) FROM supplier s WHERE 1 = 1");
        verify(jdbc).sql(contains("ORDER BY s.id ASC LIMIT :limit OFFSET :offset"));
        verify(statement, times(2)).params(Map.of("limit", 10, "offset", 0L));
        assertEquals(0, result.pageNumber());
        assertEquals(10, result.pageSize());
    }

    @Test
    @SuppressWarnings("unchecked")
    void mapsAllCategoriesAndHandlesSuppliersWithoutCategories() throws SQLException {
        prepareQueries();
        repository.search(new SupplierQuery());
        var captor = org.mockito.ArgumentCaptor.forClass(RowMapper.class);
        verify(statement).query(captor.capture());
        RowMapper<SupplierDTO> mapper = captor.getValue();
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("public_id")).thenReturn("supplier-id");
        when(rs.getString("categories")).thenReturn("GLASSWARE,TABLEWARE", "");

        SupplierDTO supplier = mapper.mapRow(rs, 0);
        assertEquals("supplier-id", supplier.publicId());
        assertEquals(Set.of(ProductCategory.GLASSWARE, ProductCategory.TABLEWARE), supplier.categories());
        assertTrue(mapper.mapRow(rs, 1).categories().isEmpty());
    }
}
