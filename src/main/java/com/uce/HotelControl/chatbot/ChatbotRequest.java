package com.uce.HotelControl.chatbot;

// Recibe el mensaje que el cliente escribe en el chat.
public class ChatbotRequest {

    private String mensaje;

    public ChatbotRequest() {
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}