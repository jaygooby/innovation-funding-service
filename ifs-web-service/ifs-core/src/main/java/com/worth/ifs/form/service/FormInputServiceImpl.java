package com.worth.ifs.form.service;

import com.worth.ifs.form.resource.FormInputResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * This class contains methods to retrieve and store {@link FormInputResource} related data,
 * through the RestService {@link FormInputRestService}.
 */
@Service
public class FormInputServiceImpl implements FormInputService {

    @Autowired
    private FormInputRestService formInputRestService;

    @Override
    public FormInputResource getOne(Long formInputId) {
        return formInputRestService.getById(formInputId).getSuccessObjectOrThrowException();
    }

    @Override
    public List<FormInputResource> findByQuestion(Long questionId) {
        return formInputRestService.getByQuestionId(questionId).getSuccessObjectOrThrowException();
    }

    @Override
    public List<FormInputResource> findByCompetitionId(Long competitionId) {
        return formInputRestService.getByCompetitionId(competitionId).getSuccessObjectOrThrowException();
    }
}
