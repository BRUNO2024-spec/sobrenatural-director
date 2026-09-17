package com.sobrenaturaldirector.research;

import java.util.List;
import java.util.Arrays;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

/** Explicit session consent command; it only gates research collection. */
public final class DirectorConsentCommand extends CommandBase {
    private final SessionConsentRegistry registry;
    public DirectorConsentCommand(SessionConsentRegistry registry) { if(registry==null)throw new IllegalArgumentException("registry required");this.registry=registry; }
    public String getCommandName(){return "director";}
    public String getCommandUsage(ICommandSender sender){return "/director consent <accept|decline|revoke|status>";}
    public int getRequiredPermissionLevel(){return 0;}
    public void processCommand(ICommandSender sender,String[] args){String key=sender.getCommandSenderName();long tick=sender instanceof net.minecraft.entity.player.EntityPlayer?((net.minecraft.entity.player.EntityPlayer)sender).worldObj.getTotalWorldTime():0;if(args.length<2||!"consent".equalsIgnoreCase(args[0])){sender.addChatMessage(new net.minecraft.util.ChatComponentText(getCommandUsage(sender)));return;}String op=args[1].toLowerCase();if("accept".equals(op)){sender.addChatMessage(new net.minecraft.util.ChatComponentText("Research consent accepted for this session; participant="+registry.accept(key,tick)));}else if("decline".equals(op)){registry.decline(key);sender.addChatMessage(new net.minecraft.util.ChatComponentText("Research consent declined; no research collection for this session."));}else if("revoke".equals(op)){registry.revoke(key,tick);sender.addChatMessage(new net.minecraft.util.ChatComponentText("Research consent revoked; new research records are disabled."));}else if("status".equals(op)){sender.addChatMessage(new net.minecraft.util.ChatComponentText("Research consent="+registry.state(key)));}else sender.addChatMessage(new net.minecraft.util.ChatComponentText(getCommandUsage(sender)));}
    public List<String> addTabCompletionOptions(ICommandSender sender,String[] args){return args.length==2?Arrays.asList("accept","decline","revoke","status"):null;}
    public boolean canCommandSenderUseCommand(ICommandSender sender){return true;}
}
