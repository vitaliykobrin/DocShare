package com.geekhub.controllers;

import com.geekhub.dto.RegistrationInfo;
import com.geekhub.dto.UserDto;
import com.geekhub.dto.UserFileDto;
import com.geekhub.dto.convertors.EntityToDtoConverter;
import com.geekhub.entities.User;
import com.geekhub.entities.UserDocument;
import com.geekhub.entities.enums.DocumentAttribute;
import com.geekhub.exceptions.UserValidateException;
import com.geekhub.security.UserProfileManager;
import com.geekhub.services.UserDocumentService;
import com.geekhub.services.UserService;
import com.geekhub.providers.UserProvider;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/main")
public class MainController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserProvider userProvider;

    @Autowired
    private UserProfileManager userProfileManager;

    @Autowired
    private UserDocumentService userDocumentService;

    @RequestMapping(value = "/home", method = RequestMethod.GET)
    public ModelAndView home() {
        return new ModelAndView("redirect:/document/upload");
    }

    @RequestMapping(value = "/sign_in", method = RequestMethod.GET)
    public ModelAndView signIn() {
        userProvider.fillDB();
        return new ModelAndView("signIn");
    }

    @RequestMapping(value = "/sign_in", method = RequestMethod.POST)
    public ModelAndView signIn(String j_username, String j_password, HttpSession session) {
        ModelAndView model = new ModelAndView();
        User user;
        try {
            user = userProfileManager.authenticateUser(j_username, j_password);
            session.setAttribute("userId", user.getId());
            session.setAttribute("parentDirectoryHash", user.getLogin());
            session.setAttribute("currentLocation", user.getLogin());
            model.setViewName("redirect:/document/upload");
        } catch (UserValidateException e) {
            model.addObject("errorMessage", e.getMessage());
            model.setViewName("singIn");
        }
        return model;
    }

    @RequestMapping(value = "/sign_up", method = RequestMethod.GET)
    public String signUp() {
        return "signUp";
    }

    @RequestMapping(value = "/sign_up", method = RequestMethod.POST)
    public ModelAndView signUp(RegistrationInfo registrationInfo)
            throws HibernateException {

        ModelAndView model = new ModelAndView();
        try {
            userProfileManager.registerNewUser(registrationInfo);
            model.setViewName("signIn");
            model.addObject("message", "Your account created successfully");
        } catch (UserValidateException e) {
            model.addObject("registrationInfo", registrationInfo)
                    .addObject("errorMessage", e.getMessage())
                    .setViewName("signUp");
        }
        return model;
    }

    @RequestMapping("/signOut")
    public String signOut(HttpSession session) {
        session.invalidate();
        return "signIn";
    }

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public ModelAndView search(HttpSession session) {
        User user = userService.getById((Long) session.getAttribute("userId"));

        List<User> users = userService.getAll("firstName");
        users.remove(user);
        Map<UserDto, Boolean> usersMap = users.stream()
                .collect(Collectors.toMap(EntityToDtoConverter::convert, u -> !userService.areFriends(user.getId(), u)));

        ModelAndView model = new ModelAndView("search");
        model.addObject("usersMap", usersMap);
        return model;
    }

    @RequestMapping(value = "/search", method = RequestMethod.POST)
    public ModelAndView search(String searchParameter, HttpSession session) {
        User user = userService.getById((Long) session.getAttribute("userId"));

        Set<User> users = userService.searchByName(searchParameter);
        users.remove(user);
        Map<UserDto, Boolean> usersMap = users.stream()
                .collect(Collectors.toMap(EntityToDtoConverter::convert, u -> !userService.areFriends(user.getId(), u)));

        ModelAndView model = new ModelAndView("search");
        model.addObject("usersMap", usersMap);
        return model;
    }

    @RequestMapping("/userpage/{ownerId}")
    public ModelAndView userPage(@PathVariable Long ownerId, HttpSession session) {
        User user = userService.getById((Long) session.getAttribute("userId"));
        User owner = userService.getById(ownerId);

        Set<UserDocument> documents = new HashSet<>();
        documents.addAll(userDocumentService.getAllByOwnerAndAttribute(owner, DocumentAttribute.PUBLIC));
        if (userService.areFriends(ownerId, user)) {
            documents.addAll(userDocumentService.getAllByOwnerAndAttribute(owner, DocumentAttribute.FOR_FRIENDS));
        }
        Set<UserFileDto> fileDtoSet = new TreeSet<>();
        documents.forEach(d -> fileDtoSet.add(EntityToDtoConverter.convert(d)));

        ModelAndView model = new ModelAndView("userPage");
        model.addObject("pageOwner", EntityToDtoConverter.convert(owner));
        model.addObject("documents", fileDtoSet);
        return model;
    }
}