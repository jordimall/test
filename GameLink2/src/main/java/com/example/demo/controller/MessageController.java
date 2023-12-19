package com.example.demo.controller;

import java.sql.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.Message;
import com.example.demo.dto.UserPartyGameRole;
import com.example.demo.security.GameLinkUserDetails;
import com.example.demo.service.MessageService;
import com.example.demo.service.PartyService;
import com.example.demo.service.UserPartyGameRoleService;
import com.example.demo.service.UserService;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/message")
public class MessageController {

	@Autowired(required = true)
	MessageService messageService;

	@Autowired(required = true)
	PartyService partyService;

	@Autowired(required = true)
	UserPartyGameRoleService userPartyGameRoleService;

	@Autowired(required = true)
	UserService userService;

	@GetMapping("/all")
	public ResponseEntity<Page<Message>> listAllMessages(
			@RequestParam(name = "idParty", required = false) Integer idParty,
			@RequestParam(name = "idUser", required = false) Integer idUser, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		Page<Message> messagePage = null;

		if (idParty != null && idUser != null) {
			messagePage = messageService.getPaginatedAllPartyAndAuthor(PageRequest.of(page, size), idParty, idUser);
		} else if (idParty != null) {
			messagePage = messageService.getPaginatedAllParty(PageRequest.of(page, size), idParty);
		} else if (idUser != null) {
			messagePage = messageService.getPaginatedAllAuthor(PageRequest.of(page, size), idUser);
		} else {
			messagePage = messageService.getPaginatedAllMessage(PageRequest.of(page, size));
		}

		return new ResponseEntity<>(messagePage, HttpStatus.OK);
	}

	// Return the messages of the party passed by path variable if the user who sent
	// the request is a member of that party
	@GetMapping("/party/{id}")
	public ResponseEntity<Page<Message>> listAllMessagerByParty(Authentication authentication,
			@PathVariable(name = "id") int id, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {

		Page<Message> messagePage = null;
		GameLinkUserDetails ud = (GameLinkUserDetails) authentication.getPrincipal();
		List<UserPartyGameRole> members = userPartyGameRoleService.findByPartyId(id);
		boolean isMember = false;

		// Check if the user who sent the request is a member of the party
		for (UserPartyGameRole player : members) {
			if (player.getUser().getId() == ud.getId()) {
				isMember = true;
			}
		}

		// Is the user who sent the request is a member of the party return the messages
		// of that party
		if (isMember) {
			messagePage = messageService.getPaginatedAllParty(PageRequest.of(page, size), id);
		}

		return new ResponseEntity<>(messagePage, HttpStatus.OK);
	}

	@PostMapping("/add")
	public Message saveMessage(Authentication authentication, @RequestBody Message message) {
		GameLinkUserDetails ud = (GameLinkUserDetails) authentication.getPrincipal();
		message.setCreated_at(new Date(System.currentTimeMillis()));
		message.setUpdated_at(new Date(System.currentTimeMillis()));
		message.setAuthor(userService.getOneById(ud.getId()));
		return messageService.add(message);
	}

	// Adds a message to the party passed by path variable if the user who sent the
	// request is a member of the party
	@PostMapping("/party/write/{id}")
	public ResponseEntity addMessage(Authentication authentication, @PathVariable(name = "id") int id,
			@RequestBody Message message) {

		GameLinkUserDetails ud = (GameLinkUserDetails) authentication.getPrincipal();
		List<UserPartyGameRole> members = userPartyGameRoleService.findByPartyId(id);
		boolean isMember = false;

		// Check if the user who sent the request is a member of the party
		for (UserPartyGameRole player : members) {
			if (player.getUser().getId() == ud.getId()) {
				isMember = true;
				message.setIdParty(player.getParty());
			}
		}

		// Is the user who sent the request is a member of the party return the messages
		// of that party
		if (isMember) {
			message.setCreated_at(new Date(System.currentTimeMillis()));
			message.setUpdated_at(new Date(System.currentTimeMillis()));
			message.setAuthor(userService.getOneById(ud.getId()));
			messageService.add(message);
		} else {
			return new ResponseEntity(HttpStatus.FORBIDDEN);
		}
		return new ResponseEntity(HttpStatus.OK);
	}

	@GetMapping("/{id}")
	public Message getOneMessage(@PathVariable(name = "id") int id) {
		return messageService.getOne(id);
	}

	@GetMapping("/id/{id}")
	public Message getOwnMessage(Authentication authentication, @PathVariable(name = "id") int id) {

		GameLinkUserDetails ud = (GameLinkUserDetails) authentication.getPrincipal();
		Message msg = messageService.getOne(id);
		if (msg.getAuthor().getId() == ud.getId()) {
			return msg;
		} else {
			return null;
		}

	}

	@PutMapping("/{id}")
	public Message updateMessage(@PathVariable(name = "id") int id, @RequestBody Message message) {

		Message prevMessage = new Message();
		Message newMessage = new Message();

		prevMessage = messageService.getOne(id);

		prevMessage.setMessage(message.getMessage());
		prevMessage.setUpdated_at(new Date(System.currentTimeMillis()));
		prevMessage.setIdParty(message.getIdParty());

		newMessage = messageService.update(prevMessage);

		return newMessage;
	}

	@PutMapping("/update/{id}")
	public Message updateOwnMessage(Authentication authentication, @PathVariable(name = "id") int id,
			@RequestBody Message message) {

		Message prevMessage = new Message();
		Message newMessage = new Message();

		GameLinkUserDetails ud = (GameLinkUserDetails) authentication.getPrincipal();

		prevMessage = messageService.getOne(id);

		if (prevMessage.getAuthor().getId() == ud.getId()) {
			prevMessage.setMessage(message.getMessage());
			prevMessage.setUpdated_at(new Date(System.currentTimeMillis()));
			newMessage = messageService.update(prevMessage);
		}

		return newMessage;
	}

	@DeleteMapping("/{id}")
	public void deleteMessage(@PathVariable(name = "id") int id) {
		messageService.deleteOne(id);
	}

	@DeleteMapping("/delete/{id}")
	public void deleteOwnMessage(Authentication authentication, @PathVariable(name = "id") int id) {
		GameLinkUserDetails ud = (GameLinkUserDetails) authentication.getPrincipal();
		Message msg = messageService.getOne(id);
		if (msg.getAuthor().getId() == ud.getId()) {
			messageService.deleteOne(id);
		}
	}

}
