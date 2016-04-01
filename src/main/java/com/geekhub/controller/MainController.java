package com.geekhub.controller;

import com.geekhub.entity.User;
import com.geekhub.exception.UserValidateException;
import com.geekhub.service.UserService;
import com.geekhub.util.UserUtil;
import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
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
    private UserUtil userUtil;

    @RequestMapping(value = "/home", method = RequestMethod.GET)
    public ModelAndView home() {
        return new ModelAndView("home");
    }

    @RequestMapping(value = "/sign_in", method = RequestMethod.GET)
    public ModelAndView signIn() {
        return new ModelAndView("signIn");
    }

    @RequestMapping(value = "/sign_in", method = RequestMethod.POST)
    public ModelAndView signIn(String j_username, String j_password, HttpSession session) {
        ModelAndView model = new ModelAndView();
        User user = userService.getByLogin(j_username);
        if (user != null && user.getPassword().equals(j_password)) {
            model.setViewName("redirect:/main/home");
            session.setAttribute("userId", user.getId());
        } else {
            model.addObject("errorMessage", "Wrong login or password")
                    .setViewName("signIn");
        }
        return model;
    }

    @RequestMapping(value = "/sign_up", method = RequestMethod.GET)
    public String signUp() {
        return "signUp";
    }

    @RequestMapping(value = "/sign_up", method = RequestMethod.POST)
    public ModelAndView signUp(String firstName,
                               String lastName,
                               String login,
                               String password,
                               String confirmPassword)
            throws HibernateException {

        ModelAndView model = new ModelAndView();
        try {
            userUtil.validateUser(login, password, confirmPassword);
            userService.createUser(firstName, lastName, login, password);
            model.setViewName("signIn");
            model.addObject("message", "Your account created successfully");
        } catch (UserValidateException e) {
            model.addObject("login", login)
                    .addObject("firstName", firstName)
                    .addObject("lastName", lastName)
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
        Long userId = (Long) session.getAttribute("userId");

        List<User> users = userService.getAllWithoutCurrentUser(userId);
        Map<User, Boolean> usersMap = users.stream()
                .collect(Collectors.toMap(u -> u, u -> !userService.areFriends(userId, u)));

        ModelAndView model = new ModelAndView("search");
        model.addObject("usersMap", usersMap);
        return model;
    }

    @RequestMapping("/userpage/{ownerId}")
    public ModelAndView userPage(@PathVariable Long ownerId) {
        User owner = userService.getById(ownerId);
        ModelAndView model = new ModelAndView("userPage");
        model.addObject("pageOwner", owner);
        return model;
    }
}