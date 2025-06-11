package com.example.onlinelearning.service;

import com.example.onlinelearning.model.Assignment;
import com.example.onlinelearning.repository.AssignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;

    @Autowired
    public AssignmentServiceImpl(AssignmentRepository assignmentRepository) {
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Assignment> findAssignmentByIdWithDetails(Long assignmentId) {
        return assignmentRepository.findByIdWithDetails(assignmentId);
    }

    // TODO: Реализовать методы для сохранения/проверки ответа пользователя
}