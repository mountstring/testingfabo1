package com.project.Fabo.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.Fabo.entity.ClientUser;
import com.project.Fabo.entity.Role;
import com.project.Fabo.entity.User;
import com.project.Fabo.entity.UsersRoles;
import com.project.Fabo.repository.ClientUserRepository;
import com.project.Fabo.repository.RoleRepository;
import com.project.Fabo.repository.UserRepository;
import com.project.Fabo.repository.UserRolesRepository;
import com.project.Fabo.service.ClientUserService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Service
public class ClientUserServiceImpl implements ClientUserService  {
	
private ClientUserRepository clientUserRepository;

private UserRepository userRepository;

private RoleRepository roleRepository;


@Autowired
private UserRolesRepository userRolesRepository;


public ClientUserServiceImpl(ClientUserRepository clientUserRepository, UserRepository userRepository,
		RoleRepository roleRepository,
		UserRolesRepository userRolesRepository, EntityManager entityManager) {
	this.clientUserRepository = clientUserRepository;
	this.userRepository = userRepository;
	this.roleRepository = roleRepository;
	this.userRolesRepository = userRolesRepository;
	this.entityManager = entityManager;
}

@PersistenceContext
private EntityManager entityManager;

@Override
public List<ClientUser> getAllUsers() {
	return clientUserRepository.findAll() ;	}

@Override
public ClientUser saveUser(ClientUser ClientUser) {
	return clientUserRepository.save(ClientUser);
}

@Override
public ClientUser getClientUserById(String id) {
	return clientUserRepository.findById(id).orElse(null);
}

@Override
public ClientUser updateUser(ClientUser ClientUser) {

	return clientUserRepository.save(ClientUser) ;
}
/*
@Transactional
public void addClientUserAndRoles(ClientUser clientUser, List<Long> roleIds) {
    // Find the existing users with the same email
    List<User> users = userRepository.findAllByUserName(clientUser.getEmail());

    // If multiple users with the same email are found, remove their roles in UsersRoles and delete the users
    if (!users.isEmpty()) {
        for (User user : users) {
            List<UsersRoles> usersRoles = userRolesRepository.findByUser(user);
            userRolesRepository.deleteAll(usersRoles);
        }
    }

    // Create a User with the same email
    User user = new User();
    user.setUserName(clientUser.getEmail()); // Assuming email is used as username

    // Save the new User only if no duplicates were found
    userRepository.save(user);

    // Save ClientUser details
    ClientUser savedClientUser = clientUserRepository.save(clientUser);

    // Assign roles to the created User
    for (Long roleId : roleIds) {
        Role role = roleRepository.findById(roleId).orElse(null);
        if (role != null) {
            UsersRoles usersRole = new UsersRoles();
            usersRole.setUser(user);
            usersRole.setRole(role);
            userRolesRepository.save(usersRole);
        }
    }
}*/

@Transactional
public void addClientUserAndRoles(ClientUser clientUser, List<Long> roleIds) {
    // Find the existing user by email
    User existingUser = userRepository.findByUserName(clientUser.getUserName());

    if (existingUser != null) {
        // If user exists, remove existing roles
        List<UsersRoles> usersRoles = userRolesRepository.findByUser(existingUser);
        userRolesRepository.deleteAll(usersRoles);
    } else {
        // Create a new user if it doesn't exist
        User newUser = new User();
        newUser.setUserName(clientUser.getUserName());
        userRepository.save(newUser);
    }

    // Save ClientUser details
    ClientUser savedClientUser = clientUserRepository.save(clientUser);

    // Assign roles to either existing or newly created User
    User user = existingUser != null ? existingUser : userRepository.findByUserName(clientUser.getUserName());

    for (Long roleId : roleIds) {
        Role role = roleRepository.findById(roleId).orElse(null);
        if (role != null) {
            UsersRoles usersRole = new UsersRoles();
            usersRole.setUser(user);
            usersRole.setRole(role);
            userRolesRepository.save(usersRole);
        }
    }
}


@Transactional
public void removeClientUserAndAssociationsByUserName(String userName) {
    // Find users by email
    List<User> users = userRepository.findAllByUserName(userName);

    for (User user : users) {
        // Find associations by user
        List<UsersRoles> usersRoles = userRolesRepository.findByUser(user);
        userRolesRepository.deleteAll(usersRoles);
    }

    // Delete clientUsers by email
   // clientUserRepository.deleteAllByUserName(userName);
}

@Override
public void updateConcatenatedRolesByEmail(String email, String concatenatedRoleNames) {
    Optional<ClientUser> optionalClientUser = clientUserRepository.findByEmail(email);

    optionalClientUser.ifPresent(clientUser -> {
        clientUser.setConcatenatedRoleNames(concatenatedRoleNames);
        clientUserRepository.save(clientUser);
    });
}


public boolean isUsernameDuplicate(String username) {
    return clientUserRepository.existsByUserName(username);
}

@Override
public Optional<ClientUser> getClientUserByEmail(String email) {
	// TODO Auto-generated method stub
	return clientUserRepository.findByEmail(email);
}

public void deleteClientUserByUserName(String userName) {
    Optional<ClientUser> clientUserOptional = clientUserRepository.findByUserName(userName);

    if (clientUserOptional.isPresent()) {
        ClientUser clientUser = clientUserOptional.get();
        clientUser.setActiveStatus(false); // Marking as inactive

        clientUserRepository.save(clientUser);
    } else {
        throw new IllegalArgumentException("ClientUser ID not found: " + userName);
    }
}

}


