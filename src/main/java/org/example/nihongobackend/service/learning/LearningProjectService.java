package org.example.nihongobackend.service.learning;

import org.example.nihongobackend.dto.request.learning.CreateLearningProjectRequest;
import org.example.nihongobackend.dto.response.learning.LearningProjectResponse;
import org.example.nihongobackend.entity.LearningProject;
import org.example.nihongobackend.entity.User;

import java.util.List;
import java.util.UUID;

public interface LearningProjectService {

    List<LearningProjectResponse> listMine();

    LearningProjectResponse create(CreateLearningProjectRequest request);

    LearningProject requireOwnedProject(User user, UUID projectId);

    void delete(UUID projectId);
}
