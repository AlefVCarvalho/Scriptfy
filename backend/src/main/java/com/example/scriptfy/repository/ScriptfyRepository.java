package com.example.scriptfy.repository;

import com.example.scriptfy.model.ScriptfyModel;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScriptfyRepository extends JpaRepository<ScriptfyModel, Long> {
    List<ScriptfyModel> findByNomeContainingIgnoreCase(String nome, Sort sort);

    List<ScriptfyModel> findByAlbumContainingIgnoreCase(String album, Sort sort);

    List<ScriptfyModel> findByArtistasContainingIgnoreCase(String artistas, Sort sort);
}
