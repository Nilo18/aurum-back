package com.aurum.main.repository;

import com.aurum.main.model.MenuItem;
import org.springframework.data.repository.ListCrudRepository;

public interface MenuItemRepository extends ListCrudRepository<MenuItem, Long> {
}
