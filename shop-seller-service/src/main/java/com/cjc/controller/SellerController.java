package com.cjc.controller;

import com.cjc.dto.SellerRegisterDto;
import com.cjc.dto.SellerUpdateDto;
import com.cjc.pojo.TbSeller;
import com.cjc.query.QueryParams;
import com.cjc.service.SellerService;
import com.cjc.util.JwtUtil;
import com.cjc.util.PageList;
import com.cjc.util.Result;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@Tag(name = "商家管理",description = "商家管理接口")
@RestController
@RequestMapping("/seller")
public class SellerController {
    @Autowired
    private SellerService sellerService;
    
    // ========== 商家端接口 ==========
    
    @GetMapping("/queryByUsername")
    public TbSeller queryByUsername(@RequestParam String username){
        TbSeller tbSeller = sellerService.queryByUsername(username);
        return tbSeller;
    }
    
    @PostMapping("/register")
    public Result register(@RequestBody SellerRegisterDto registerDto) {
        sellerService.register(registerDto);
        return Result.success();
    }
    
    @PostMapping("/update")
    public Result update(@RequestBody SellerUpdateDto updateDto,HttpServletRequest request){
        String token = request.getHeader("token");
        Claims claims = JwtUtil.parseJwt(token);
        String sellerId = claims.getId();
        sellerService.update(updateDto,sellerId);
        return Result.success();
    }
    
    @PostMapping("/updatePassword")
    public Result updatePassword(
            @RequestBody Map<String, String> params,
            HttpServletRequest request
    ) {
        String token = request.getHeader("token");
        Claims claims = JwtUtil.parseJwt(token);
        String sellerId = claims.getId();
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        sellerService.updatePassword(sellerId, oldPassword, newPassword);
        return Result.success();
    }
    
    // ========== 运营商端接口 ==========
    
    /**
     * 运营商 - 分页查询商家列表
     */
    @PostMapping("/admin/queryPage")
    public Result queryPage(@RequestBody QueryParams<TbSeller> params) {
        PageList<TbSeller> pageList = sellerService.queryPage(params);
        return Result.success(pageList);
    }
    
    /**
     * 运营商 - 查询商家详情
     */
    @GetMapping("/admin/queryById/{sellerId}")
    public Result queryById(@PathVariable String sellerId) {
        TbSeller seller = sellerService.queryById(sellerId);
        return Result.success(seller);
    }
    
    /**
     * 运营商 - 审核商家
     */
    @PostMapping("/admin/audit")
    public Result audit(@RequestBody Map<String, String> params) {
        String sellerId = params.get("sellerId");
        String status = params.get("status");
        sellerService.audit(sellerId, status);
        return Result.success();
    }
}
