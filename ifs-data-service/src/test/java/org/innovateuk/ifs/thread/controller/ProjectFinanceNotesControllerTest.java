package org.innovateuk.ifs.thread.controller;

import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.project.finance.controller.ProjectFinanceNotesController;
import org.innovateuk.ifs.project.finance.controller.ProjectFinanceQueriesController;
import org.innovateuk.threads.resource.NoteResource;
import org.innovateuk.threads.resource.PostResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.MediaType;

import static java.util.Arrays.asList;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.util.JsonMappingUtil.toJson;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(MockitoJUnitRunner.class)
public class ProjectFinanceNotesControllerTest extends BaseControllerMockMVCTest<ProjectFinanceNotesController> {

    @Override
    protected ProjectFinanceNotesController supplyControllerUnderTest() {
        return new ProjectFinanceNotesController(projectFinanceNotesService);
    }

    @Override
    public void setupMockMvc() {
        controller = new ProjectFinanceNotesController(projectFinanceNotesService);
        super.setupMockMvc();
    }

    @Test
    public void testFindOne() throws Exception {
        final Long noteId = 22L;
        NoteResource note = new NoteResource(noteId, null, null, null, null);
        when(projectFinanceNotesService.findOne(noteId)).thenReturn(serviceSuccess(note));

        mockMvc.perform(get("/project/finance/notes/{threadId}", noteId))
                .andExpect(status().isOk())
                .andExpect(content().json(toJson(note)));

        verify(projectFinanceNotesService).findOne(22L);
    }

    @Test
    public void testFindAll() throws Exception {
        final Long contextId = 22L;
        NoteResource note = new NoteResource(3L, null, null, null, null);
        when(projectFinanceNotesService.findAll(contextId)).thenReturn(serviceSuccess(asList(note)));

        mockMvc.perform(get("/project/finance/notes/all/{contextClassId}", contextId))
                .andExpect(status().isOk())
                .andExpect(content().json(toJson(asList(note))));

        verify(projectFinanceNotesService).findAll(contextId);
    }

    @Test
    public void testCreate() throws Exception {
        final Long contextId = 22L;
        final NoteResource note = new NoteResource(35L, contextId, null, null, null);
        when(projectFinanceNotesService.create(note)).thenReturn(serviceSuccess(note.id));

        mockMvc.perform(post("/project/finance/notes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(note)))
                .andExpect(content().string(objectMapper.writeValueAsString(35L)))
                .andExpect(status().isCreated());

        verify(projectFinanceNotesService).create(note);
    }

    @Test
    public void testAddPost() throws Exception {
        Long threadId = 22L;
        PostResource post = new PostResource(33L, null, null, null, null);
        when(projectFinanceNotesService.addPost(post, threadId)).thenReturn(serviceSuccess());

        mockMvc.perform(post("/project/finance/notes/{threadId}/post", threadId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(post)))
                .andExpect(status().isCreated());

        verify(projectFinanceNotesService).addPost(post, threadId);
    }
}
