package com.aurum.main.repository;

import com.aurum.main.model.MenuItem;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;
import java.util.Optional;

public interface MenuItemRepository extends ListCrudRepository<MenuItem, Long> {
    int deleteByPublicId(String publicId);
    Optional<MenuItem> findByPublicId(String publicId);
    List<MenuItem> findAllByPublicIdIn(List<String> publicIds);
}
