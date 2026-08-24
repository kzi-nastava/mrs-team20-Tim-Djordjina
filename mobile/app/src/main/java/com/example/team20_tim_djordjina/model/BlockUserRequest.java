package com.example.team20_tim_djordjina.model;

/* Body for POST api/admin/user/{id}/block */
public class BlockUserRequest {
    private String blockNote;

    public BlockUserRequest(String blockNote) {
        this.blockNote = blockNote;
    }

    public String getBlockNote() {
        return blockNote;
    }

    public void setBlockNote(String blockNote) {
        this.blockNote = blockNote;
    }
}
