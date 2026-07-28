package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.BlockUserDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.UserListItemDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.Role;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service.AdminUserService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    /**
     * GET /api/admin/users             -> all non-admin users
     * GET /api/admin/users?role=DRIVER -> drivers only
     * GET /api/admin/users?role=USER   -> passengers only
     * */
    @GetMapping
    public ResponseEntity<List<UserListItemDTO>> listUsers(@RequestParam(required = false) Role role) {
        return ResponseEntity.ok(adminUserService.listUsers(role));
    }

    @PostMapping("/{id}/block")
    public ResponseEntity<UserListItemDTO> blockUser(@PathVariable Long id, @Valid @RequestBody BlockUserDTO request){
        return ResponseEntity.ok(adminUserService.blockUser(id, request.getBlockNote()));
    }

    @PostMapping("/{id}/unblock")
    public ResponseEntity<UserListItemDTO> unblockUser(@PathVariable Long id){
        return ResponseEntity.ok(adminUserService.unblockUser(id));
    }

}
