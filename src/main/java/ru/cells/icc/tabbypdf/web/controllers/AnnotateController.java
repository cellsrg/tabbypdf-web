package ru.cells.icc.tabbypdf.web.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.cells.icc.tabbypdf.web.controllers.responseentities.TableLocations;
import ru.cells.icc.tabbypdf.web.services.AnnotationService;

@RestController()
@RequestMapping("**/api/annotate")
public class AnnotateController {

    @Autowired
    AnnotationService annotationService;

    @PostMapping
    public String annotate(@RequestBody TableLocations data) {
        return annotationService.getAnnotatedTableLocations(data).getId().toString();
    }
}
