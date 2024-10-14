package com.example.application.views.lobby;

import com.example.application.chat.Channel;
import com.example.application.chat.ChatService;
import com.example.application.views.MainLayout;
import com.example.application.views.channel.ChannelView;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;


@Route(value = "", layout = MainLayout.class)
@PageTitle("Lobby")
public class LobbyView extends VerticalLayout {
    private final ChatService chatService;
    private final VirtualList<Channel> channels;
    private final TextField channelNameField;
    private final Button addChannelButton;

    public LobbyView(ChatService chatService){
        this.chatService = chatService;
        setSizeFull();

         channels = new VirtualList<>();
         channels.setRenderer(new ComponentRenderer<>(this::createChannelComponent));
         add(channels);
         expand(channels);

         channelNameField = new TextField();
         channelNameField.setPlaceholder("New Channel Name");

         addChannelButton = new Button("Add Channel", event -> addChannel());
         addChannelButton.setDisableOnClick(true);

         var toolbar = new HorizontalLayout(channelNameField, addChannelButton);
         toolbar.setWidthFull();
         toolbar.expand(channelNameField);
         add(toolbar);
    }

    private void refreshChannels(){
        channels.setItems(chatService.channels());
    }

    @Override
    protected void onAttach(AttachEvent attachEvent){
        refreshChannels();
    }

    private void addChannel(){
        try {
            var nameOfNewChannel = channelNameField.getValue();
            if (!nameOfNewChannel.isBlank()) {
                chatService.createChannel(nameOfNewChannel);
                channelNameField.clear();
                refreshChannels();
            }
        } finally {
            addChannelButton.setEnabled(true);
        }
    }

    private Component createChannelComponent(Channel channel){
        return new RouterLink(channel.name(), ChannelView.class, channel.id());
    }

}
