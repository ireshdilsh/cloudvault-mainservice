package com.cloudvault.mainservice.controller;
import com.cloudvault.mainservice.entity.UserSettings;
import com.cloudvault.mainservice.repository.UserSettingsRepository;
import com.cloudvault.mainservice.security.CurrentUserService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/v1/settings") @RequiredArgsConstructor
public class SettingsController {
 private final CurrentUserService current; private final UserSettingsRepository settings;
 @GetMapping("/notifications") public Map<String,Object> notifications(){return Map.of("data",notification(get()));}
 @PutMapping("/notifications") @Transactional public Map<String,Object> updateNotifications(@RequestBody Map<String,Boolean> r){UserSettings s=get();s.setMemoryReminders(r.getOrDefault("memoryReminders",s.isMemoryReminders()));s.setAccountActivity(r.getOrDefault("accountActivity",s.isAccountActivity()));s.setProductUpdates(r.getOrDefault("productUpdates",s.isProductUpdates()));return Map.of("data",notification(s));}
 @GetMapping("/privacy") public Map<String,Object> privacy(){return Map.of("data",privacy(get()));}
 @PutMapping("/privacy") @Transactional public Map<String,Object> updatePrivacy(@RequestBody Map<String,Boolean> r){UserSettings s=get();s.setPrivateMemories(r.getOrDefault("privateMemories",s.isPrivateMemories()));s.setProfileVisible(r.getOrDefault("profileVisible",s.isProfileVisible()));s.setActivityVisible(r.getOrDefault("activityVisible",s.isActivityVisible()));return Map.of("data",privacy(s));}
 @GetMapping("/appearance") public Map<String,Object> appearance(){return Map.of("data",Map.of("theme",get().getTheme()));}
 @PutMapping("/appearance") @Transactional public Map<String,Object> updateAppearance(@RequestBody Map<String,UserSettings.Theme> r){UserSettings s=get(); if(r.get("theme")!=null)s.setTheme(r.get("theme"));return Map.of("data",Map.of("theme",s.getTheme()));}
 private UserSettings get(){Long id=current.getCurrentUserId();return settings.findById(id).orElseGet(()->{UserSettings s=new UserSettings();s.setUserId(id);return settings.save(s);});}
 private Map<String,Boolean> notification(UserSettings s){return Map.of("memoryReminders",s.isMemoryReminders(),"accountActivity",s.isAccountActivity(),"productUpdates",s.isProductUpdates());}private Map<String,Boolean> privacy(UserSettings s){return Map.of("privateMemories",s.isPrivateMemories(),"profileVisible",s.isProfileVisible(),"activityVisible",s.isActivityVisible());}
}
