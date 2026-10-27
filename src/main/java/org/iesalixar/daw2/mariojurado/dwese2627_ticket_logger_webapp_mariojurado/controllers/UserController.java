package org.iesalixar.daw2.mariojurado.dwese2627_ticket_logger_webapp_mariojurado.controllers;

import jakarta.validation.Valid;
import org.iesalixar.daw2.mariojurado.dwese2627_ticket_logger_webapp_mariojurado.daos.RoleDAO;
import org.iesalixar.daw2.mariojurado.dwese2627_ticket_logger_webapp_mariojurado.daos.UserDAO;
import org.iesalixar.daw2.mariojurado.dwese2627_ticket_logger_webapp_mariojurado.entities.Role;
import org.iesalixar.daw2.mariojurado.dwese2627_ticket_logger_webapp_mariojurado.entities.User;
import org.iesalixar.daw2.mariojurado.dwese2627_ticket_logger_webapp_mariojurado.services.PasswordHasher;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserDAO userDAO;
    private final RoleDAO roleDAO;
    private final PasswordHasher passwordHasher;
    private final MessageSource messageSource;

    public UserController(UserDAO userDAO, RoleDAO roleDAO, PasswordHasher passwordHasher,
                          MessageSource messageSource) {
        this.userDAO = userDAO;
        this.roleDAO = roleDAO;
        this.passwordHasher = passwordHasher;
        this.messageSource = messageSource;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("users", userDAO.findAll());
        return "views/user/user-list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("user", new User());
        prepareForm(model, List.of());
        return "views/user/user-form";
    }

    @GetMapping("/edit")
    public String editForm(@RequestParam Long id, Model model, RedirectAttributes redirectAttributes,
                           Locale locale) {
        User user = userDAO.findById(id);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage("msg.user.notFound", null, locale));
            return "redirect:/users";
        }
        model.addAttribute("user", user);
        prepareForm(model, user.getRoles().stream().map(Role::getId).toList());
        return "views/user/user-form";
    }

    @PostMapping("/insert")
    public String insert(@Valid @ModelAttribute("user") User user, BindingResult result,
                         @RequestParam(name = "password", required = false) String password,
                         @RequestParam(name = "roleIds", required = false) List<Long> roleIds,
                         Model model, RedirectAttributes redirectAttributes, Locale locale) {
        List<Long> selectedRoleIds = distinctIds(roleIds);
        boolean hasPasswordError = password == null || password.isBlank();
        boolean hasRoleError = !containsOnlyAvailableRoles(selectedRoleIds);
        if (userDAO.existsByUsername(user.getUsername() == null ? "" : user.getUsername().trim())) {
            result.rejectValue("username", "msg.user.username.duplicate");
        }
        if (user.getLastPasswordChange() == null && !result.hasFieldErrors("lastPasswordChange")) {
            result.rejectValue("lastPasswordChange", "msg.user.lastPasswordChange.required");
        }
        if (hasPasswordError || hasRoleError) {
            setFormErrors(model, hasPasswordError, hasRoleError, locale);
        }
        if (result.hasErrors() || hasPasswordError || hasRoleError) {
            user.setUsername(user.getUsername() == null ? null : user.getUsername().trim());
            prepareForm(model, selectedRoleIds);
            return "views/user/user-form";
        }

        user.setUsername(user.getUsername().trim());
        user.setPasswordHash(passwordHasher.hash(password));
        LocalDateTime passwordChangedAt = user.getLastPasswordChange();
        user.setPasswordExpiresAt(passwordChangedAt.plusMonths(3));
        if (user.getFailedLoginAttempts() == null) {
            user.setFailedLoginAttempts(0);
        }
        user.setActive(Boolean.TRUE.equals(user.getActive()));
        user.setAccountNonLocked(Boolean.TRUE.equals(user.getAccountNonLocked()));
        user.setEmailVerified(Boolean.TRUE.equals(user.getEmailVerified()));
        user.setMustChangePassword(Boolean.TRUE.equals(user.getMustChangePassword()));
        userDAO.insert(user, selectedRoleIds);
        redirectAttributes.addFlashAttribute("successMessage",
                messageSource.getMessage("msg.user.created", null, locale));
        return "redirect:/users";
    }

    @PostMapping("/update")
    public String update(@Valid @ModelAttribute("user") User user, BindingResult result,
                         @RequestParam(name = "password", required = false) String password,
                         @RequestParam(name = "roleIds", required = false) List<Long> roleIds,
                         Model model, RedirectAttributes redirectAttributes, Locale locale) {
        User current = userDAO.findById(user.getId());
        if (current == null) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage("msg.user.notFound", null, locale));
            return "redirect:/users";
        }

        List<Long> selectedRoleIds = distinctIds(roleIds);
        boolean hasRoleError = !containsOnlyAvailableRoles(selectedRoleIds);
        String username = user.getUsername() == null ? "" : user.getUsername().trim();
        if (userDAO.existsByUsernameAndNotId(username, user.getId())) {
            result.rejectValue("username", "msg.user.username.duplicate");
        }
        if (hasRoleError) {
            setFormErrors(model, false, true, locale);
        }
        if (result.hasErrors() || hasRoleError) {
            user.setUsername(username);
            prepareForm(model, selectedRoleIds);
            return "views/user/user-form";
        }

        user.setUsername(username);
        boolean passwordChanged = password != null && !password.isBlank();
        user.setPasswordHash(passwordChanged ? passwordHasher.hash(password) : current.getPasswordHash());
        LocalDateTime passwordChangedAt = passwordChanged ? LocalDateTime.now() : current.getLastPasswordChange();
        user.setLastPasswordChange(passwordChangedAt);
        user.setPasswordExpiresAt(passwordChanged
                ? passwordChangedAt.plusMonths(3) : current.getPasswordExpiresAt());
        user.setFailedLoginAttempts(current.getFailedLoginAttempts());
        user.setActive(Boolean.TRUE.equals(user.getActive()));
        user.setAccountNonLocked(Boolean.TRUE.equals(user.getAccountNonLocked()));
        user.setEmailVerified(Boolean.TRUE.equals(user.getEmailVerified()));
        user.setMustChangePassword(Boolean.TRUE.equals(user.getMustChangePassword()));
        userDAO.update(user, selectedRoleIds);
        redirectAttributes.addFlashAttribute("successMessage",
                messageSource.getMessage("msg.user.updated", null, locale));
        return "redirect:/users";
    }

    @PostMapping("/delete")
    public String delete(@RequestParam Long id, RedirectAttributes redirectAttributes, Locale locale) {
        userDAO.delete(id);
        redirectAttributes.addFlashAttribute("successMessage",
                messageSource.getMessage("msg.user.deleted", null, locale));
        return "redirect:/users";
    }

    private void prepareForm(Model model, List<Long> selectedRoleIds) {
        model.addAttribute("availableRoles", roleDAO.findAll());
        model.addAttribute("selectedRoleIds", selectedRoleIds);
    }

    private void setFormErrors(Model model, boolean passwordError, boolean roleError, Locale locale) {
        if (passwordError) {
            model.addAttribute("passwordError", messageSource.getMessage("msg.user.password.required", null, locale));
        }
        if (roleError) {
            model.addAttribute("roleError", messageSource.getMessage("msg.user.roles.required", null, locale));
        }
    }

    private boolean containsOnlyAvailableRoles(List<Long> roleIds) {
        Set<Long> available = roleDAO.findAll().stream().map(Role::getId).collect(Collectors.toSet());
        return !roleIds.isEmpty() && available.containsAll(roleIds);
    }

    private List<Long> distinctIds(List<Long> roleIds) {
        return roleIds == null ? List.of() : roleIds.stream().filter(id -> id != null).distinct().toList();
    }
}
