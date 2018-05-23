package ru.belogurow.socialnetworkserver.chat.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;
import ru.belogurow.socialnetworkserver.chat.model.ChatMessage;
import ru.belogurow.socialnetworkserver.chat.model.ChatRoom;
import ru.belogurow.socialnetworkserver.chat.service.ChatMessageService;
import ru.belogurow.socialnetworkserver.chat.service.ChatRoomService;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@RestController
public class ChatController {

    private static Logger LOGGER = LoggerFactory.getLogger(ChatController.class);

    private ChatRoomService chatRoomService;
    private ChatMessageService chatMessageService;

    @RequestMapping(value = "/chat", method = RequestMethod.POST)
    public ResponseEntity createChat(@RequestBody ChatRoom chatRoom) {
        LOGGER.info("createChat({})", chatRoom);

        Optional<ChatRoom> chatRoomResult = chatRoomService.findByUserIds(chatRoom.getFirstUserId(), chatRoom.getSecondUserId());
        return chatRoomResult.<ResponseEntity>map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.ok(chatRoomService.save(chatRoom)));
    }

    @RequestMapping(value = "/chat/{userId}", method = RequestMethod.GET)
    public ResponseEntity getAllChatsByUserId(@PathVariable(value = "userId") UUID userId) {
        LOGGER.info("getAllChatsByUserId({})", userId);

        return ResponseEntity.ok(chatRoomService.findAllByUserId(userId));
    }

    @RequestMapping(value = "/chat/{chatId}/messages", method = RequestMethod.GET)
    public ResponseEntity getAllMessagesByChatId(@PathVariable("chatId") UUID chatId) {
        LOGGER.info("getAllMessagesByChatId({})", chatId);

        return ResponseEntity.ok(chatMessageService.getAllMessagesByChatRoomId(chatId));
    }

    @Deprecated
    @MessageMapping("/chatRoom/{id}")
    @SendTo("/topic/messages/{id}")
    public ChatMessage message(@DestinationVariable("id") UUID id, String textMessage) {
        LOGGER.info("message({}, {})", id, textMessage);
        LOGGER.info("message({})", textMessage);

        ChatMessage message = new ChatMessage();
        message.setId(UUID.randomUUID());
        message.setDate(new Date());
        message.setText(textMessage + "DEPRECATED");
        return message;
    }

    @MessageMapping("/chatRoom/{chatId}/{authorId}")
    @SendTo("/topic/chatRoom/{chatId}/messages")
    public ChatMessage sendMessage(@DestinationVariable("chatId") String chatId,
                                   @DestinationVariable("authorId") String authorId,
                                   String textMessage) {
        LOGGER.info("sendMessage({}, {}, {})", chatId, authorId, textMessage);


        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setAuthorId(UUID.fromString(authorId));
        chatMessage.setChatRoomId(UUID.fromString(chatId));
        chatMessage.setDate(new Date());
        chatMessage.setText(textMessage);

        return chatMessageService.save(chatMessage);
    }

    @Autowired
    public void setChatRoomService(ChatRoomService chatRoomService) {
        this.chatRoomService = chatRoomService;
    }

    @Autowired
    public void setChatMessageService(ChatMessageService chatMessageService) {
        this.chatMessageService = chatMessageService;
    }
}