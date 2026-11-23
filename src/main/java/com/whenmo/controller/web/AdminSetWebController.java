package com.whenmo.controller.web;

import com.whenmo.controller.helper.SecurityHelper;
import com.whenmo.controller.helper.SecurityHelper.RequestAuth;
import com.whenmo.model.database.response.BoardResponse;
import com.whenmo.service.database.BoardRestService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminSetWebController {

    private final SecurityHelper securityHelper;
    private final BoardRestService boardRestService;

    /**
     * 公告配置
     * GET /admin/board
     */
    @GetMapping("/board")
    public String boardSettingPage(HttpServletRequest httpRequest, Model model) {
        String redirectPath = securityHelper.auth(httpRequest).resolveRedirect(true);
        if (redirectPath != null)
            return redirectPath;

        return "admin/boards";
    }

    /**
     * 新增公告
     * GET /admin/board/create
     */
    @GetMapping("/board/create")
    public String createBoardPage(HttpServletRequest httpRequest, Model model) {
        RequestAuth requestAuth = securityHelper.auth(httpRequest);
        String redirectPath = requestAuth.resolveRedirect(true);
        if (redirectPath != null)
            return redirectPath;

        model.addAttribute("userId", requestAuth.getUserId());
        model.addAttribute("board", null);
        return "admin/board-edit";
    }

    /**
     * 編輯公告
     * GET /admin/board/{boardId}/edit
     */
    @GetMapping("/board/{boardId}/edit")
    public String editBoardPage(HttpServletRequest httpRequest, @PathVariable Integer boardId, Model model) {
        RequestAuth requestAuth = securityHelper.auth(httpRequest);
        String redirectPath = requestAuth.resolveRedirect(true);
        if (redirectPath != null)
            return redirectPath;

        BoardResponse board = boardRestService.getBoardById(boardId);
        if (board == null) {
            return "redirect:/admin/board";
        }

        model.addAttribute("userId", requestAuth.getUserId());
        model.addAttribute("board", board);
        return "admin/board-edit";
    }

    /**
     * 數據庫配置
     * GET /admin/cdb
     */
    @GetMapping("/cdb")
    public String cdbSettingPage(HttpServletRequest httpRequest, Model model) {
        String redirectPath = securityHelper.auth(httpRequest).resolveRedirect(true);
        if (redirectPath != null)
            return redirectPath;

        return "admin/cdb";
    }

    /**
     * 公共卡包配置
     * GET /admin/pack
     */
    @GetMapping("/pack")
    public String packSettingPage(HttpServletRequest httpRequest, Model model) {
        String redirectPath = securityHelper.auth(httpRequest).resolveRedirect(true);
        if (redirectPath != null)
            return redirectPath;

        return "admin/packs";
    }

    /**
     * 用戶配置
     * GET /admin/user
     */
    @GetMapping("/user")
    public String userSettingPage(HttpServletRequest httpRequest, Model model) {
        String redirectPath = securityHelper.auth(httpRequest).resolveRedirect(true);
        if (redirectPath != null)
            return redirectPath;

        return "admin/user";
    }

    /**
     * 作者配置
     * GET /admin/diyer
     */
    @GetMapping("/diyer")
    public String diyerSettingPage(HttpServletRequest httpRequest, Model model) {
        String redirectPath = securityHelper.auth(httpRequest).resolveRedirect(true);
        if (redirectPath != null)
            return redirectPath;

        return "admin/diyer";
    }
}