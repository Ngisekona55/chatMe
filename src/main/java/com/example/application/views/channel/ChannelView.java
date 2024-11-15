package com.example.application.views.channel;

import com.example.application.chat.ChatService;
import com.example.application.chat.Message;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.example.application.views.lobby.LobbyView;


import java.util.ArrayList;
import java.util.List;

@Route(value = "channel", layout = MainLayout.class)
public class ChannelView extends VerticalLayout
        implements HasUrlParameter<String>, HasDynamicTitle {

    private String channelName;

    private final ChatService chatService;
    private final MessageList messageList;
    private String channelId;
    private final List<Message> receivedMessages = new ArrayList<>();

    //constructor
    public ChannelView(ChatService chatService){
        this.chatService = chatService;
        setSizeFull();

        messageList = new MessageList();
        messageList.setSizeFull();
        add(messageList);

        var messageInput = new MessageInput();
        messageInput.setWidthFull();
        add(messageInput);

        messageInput.addSubmitListener(event -> sendMessage(event.getValue()));
    }

    @Override
    public void setParameter(BeforeEvent event, String channelId){
        chatService.channel(channelId).ifPresentOrElse(
                channel -> this.channelName = channel.name(),
                () -> event.forwardTo(LobbyView.class)
        );
        this.channelId = channelId;
//        if(chatService.channel(channelId).isEmpty()){
//            event.forwardTo(LobbyView.class);
//        } else{
//        this.channelId = channelId;
//      }
    }

    private void sendMessage(String message){
        if(!message.isBlank()){
            chatService.postMessage(channelId, message);
        }
    }

    private MessageListItem createMessageListItem(Message message){
        var item = new MessageListItem(
                message.message(),
                message.timestamp(),
                message.author()
        );
        return item;
    }

    private void receiveMessages(List<Message> incoming){
        getUI().ifPresent(ui -> ui.access(() -> {
            receivedMessages.addAll(incoming);
            messageList.setItems(receivedMessages.stream()
                    .map(this::createMessageListItem)
                    .toList()
            );
        }));
    }

    public reactor.core.Disposable subscribe(){
        var subscription = chatService
                .liveMessages(channelId)
                .subscribe(this::receiveMessages);

        return subscription;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent){
        var subscription = subscribe();
        addAttachListener(event -> subscription.dispose());
    }

    public String getPageTitle(){
        return channelName;
    }

}
