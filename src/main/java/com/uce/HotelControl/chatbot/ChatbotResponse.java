package com.uce.HotelControl.chatbot;

// Devuelve la respuesta generada para mostrarla en el chat.
public class ChatbotResponse {

    private String respuesta;

    public ChatbotResponse() {
    }

    public ChatbotResponse(String respuesta) {
        this.respuesta = respuesta;
    }

    public String getRespuesta() {
        return respuesta;
    }

    public void setRespuesta(String respuesta) {
        this.respuesta = respuesta;
    }
}
