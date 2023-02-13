package com.nbcamp.gamematching.matchingservice.config;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Component
public class DiscordJdaConfig {
    private JDA jda;
    @Value("${jda.discord.guild.id}")
    private String guildId;
    @Value("${jda.discord.guild.category.Egid}")
    private String EgcategoryId;
    @Value("${jda.discord.guild.category.Hgid}")
    private String HgcategoryId;


    public DiscordJdaConfig(@Value("${jda.discord.key}") String discordToken) {
        try {
            jda = JDABuilder.createDefault(discordToken)
                    .setStatus(OnlineStatus.ONLINE)
                    .setChunkingFilter(ChunkingFilter.ALL)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS)
                    .setMemberCachePolicy(MemberCachePolicy.ONLINE)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //디스코드 아이디 체크 : ex) 리릭#1633
    public boolean checkMember(String discordId) {
        Optional<User> getMember = Optional.ofNullable(jda.getUserByTag(discordId));
        if (getMember.isPresent()) {
            return true;
        }
        return false;
    }

    public String createVoiceChannel(String category, List<String> discordIdList)
            throws ExecutionException, InterruptedException {
        String channelUrl = "";
        Guild guild = jda.getGuildById(guildId);

        try {
            switch (category) {
                case ("ㅈㄱ"):
                    return getUrl("ㅈㄱ", discordIdList, channelUrl, guild, EgcategoryId);

                case ("ㅃㄱ"):
                    return getUrl("ㅃㄱ", discordIdList, channelUrl, guild, HgcategoryId);
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        return channelUrl;
    }

    private String getUrl (String channelName, List <String> discordIdList, String channelUrl,
                           Guild guild, String categoryId){
        Category category = guild.getCategoryById(categoryId);
        try {
            VoiceChannel voiceChannel = category.createVoiceChannel(channelName)
                    .addPermissionOverride(guild.getPublicRole(),
                            EnumSet.of(Permission.VOICE_CONNECT),
                            EnumSet.of(Permission.VIEW_CHANNEL))
                    .reason("매칭 완료 방 생성").submit().get();
            for (String userTag : discordIdList) {
                Member member = guild.getMemberByTag(userTag);
                //각 멤버 채널 초대
                voiceChannel.createPermissionOverride(member).setAllow(Permission.VIEW_CHANNEL).queue();
            }
            voiceChannel.getManager().setUserLimit(5).queue();
            channelUrl = voiceChannel.createInvite().setMaxAge(300).submit().get().getUrl();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return channelUrl;
    }


    public void deleteVoiceChannel() {
        Guild guild = jda.getGuildById(guildId);
        System.out.println("현재 길드(서버) : " + guild.toString());

        List<VoiceChannel> channelList = guild.getVoiceChannels();

        System.out.println("채널 목록   ------> ");
        for (VoiceChannel guildChannel : channelList) {
            System.out.print(guildChannel);

            List<Member> memberList = guildChannel.getMembers();
            if (memberList.size() == 0 || memberList.isEmpty()) {
                System.out.print(" ---> 삭제완료");
                guildChannel.delete().reason("사용자가 없으므로 제거").queue();
            }
            System.out.println();
        }
        System.out.println("채널 목록 끝 ------ ");
    }
}