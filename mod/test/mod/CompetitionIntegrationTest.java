// SPDX-License-Identifier: GPL-3.0-only
package mod;

import best.I;
import best.L;
import best.ah;
import best.at;
import best.i;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import java.lang.reflect.Field;
import java.awt.Component;
import java.awt.Container;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.*;
import javax.swing.*;
import static mod.CompetitionMod.*;

/** Deterministic integration checks through the game's actual fixture/save classes. */
@SuppressWarnings({"unchecked","rawtypes"})
public class CompetitionIntegrationTest {
    static int checks;
    static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    static void layout(Component component){if(component instanceof Container){((Container)component).doLayout();for(Component child:((Container)component).getComponents())layout(child);}}
    static void render(JComponent component,String name,int width,int height)throws Exception{
        String output=System.getProperty("mod.test.output");if(output==null)return;
        SwingUtilities.invokeAndWait(()->{
            component.setSize(width,height);layout(component);
            BufferedImage image=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D graphics=image.createGraphics();component.printAll(graphics);graphics.dispose();
            try{javax.imageio.ImageIO.write(image,"png",Paths.get(output,name).toFile());}
            catch(java.io.IOException e){throw new RuntimeException(e);}
        });
    }
    static void field(Object target,String name,Object value)throws Exception{
        Class<?> type=target.getClass();
        while(type!=null){try{Field f=type.getDeclaredField(name);f.setAccessible(true);f.set(target,value);return;}catch(NoSuchFieldException ignored){type=type.getSuperclass();}}
        throw new NoSuchFieldException(name);
    }
    static ah club(String ref,int country)throws Exception{
        ah club=new ah();field(club,"mV",ref);field(club,"dm",ref);
        club.setPais(country);club.bX(c.a.SR.P().size());c.a.SR.c(club);return club;
    }
    static void winner(at cup,int season,ah club)throws Exception{
        i record=new i();field(record,"ae",season);field(record,"bQ",club.lk());cup.mn().add(record);
    }
    static Intercontinental fixture(int edition)throws Exception{
        new best.M(true);
        new c.a();c.a.SR.setJogaIntClubes(true);c.a.SR.bs=false;c.a.SR.I(2022);
        c.a.SR.k(edition-2022+1);
        // Several countries per confederation; three US clubs exercise the host exemption.
        int[][] countries={{97,65,104,3,72,154,15},{29,11,46,195},{57,1,190,129},{9,107,49,39},{68,131,51},{143,69}};
        for(int[] confed:countries)for(int country:confed)for(int n=0;n<3;n++)club("team_"+country+"_"+n,country);
        club("usa_host_candidate",68);
        club("chelsea",97);club("palmeiras",29);club("al_hilal",9);club("alahly_egi",57);club("monterrey_mex",131);
        c.a.SR.a(new f.v());c.a.SR.a(new f.r());c.a.SR.a(new f.t());c.a.SR.a(new f.u());c.a.SR.a(new f.w());c.a.SR.a(new f.y());
        Intercontinental root=state();
        for(int conf=0;conf<6;conf++){
            ArrayList<ah> members=new ArrayList<>();for(ah club:clubs())if(club.gg()==conf)members.add(club);
            check(!members.isEmpty(),"Confederação existente: "+conf);
            for(int year=2022;year<edition;year++){
                winner(continents()[conf],year-2022+1,members.get((year-2022)%Math.min(3,members.size())));
                for(int n=0;n<members.size();n++){
                    SeasonScore score=new SeasonScore();score.year=year;score.confed=conf;score.clubId=members.get(n).lk();score.wins=20-n;score.draws=1;score.placement=n+1;score.origin="fixture";root.history.add(score);
                }
            }
            winner(continents()[conf],edition-2022+1,members.get(0));
        }
        c.a.SR.r(edition);best.a.g();
        return root;
    }
    static void score(I match,int home,int away)throws Exception{
        field(match,"fB",home);field(match,"fC",away);match.a(true);
    }
    static void playDay(int n)throws Exception{
        c.a.SR.l(n);best.a day=day(n);
        for(Object object:day.h())score((I)object,2,0);
        day.a(true);
        for(Object phase:new ArrayList<>(day.j())){
            if(phase instanceof Groups)((Groups)phase).pO();
            else if(phase instanceof Stage)((Stage)phase).pO();
        }
    }
    static byte[] serialize(Object object){
        Kryo kryo=new Kryo();kryo.setRegistrationRequired(false);
        Output output=new Output(8192,-1);kryo.writeClassAndObject(output,object);return output.toBytes();
    }
    static Object deserialize(byte[] bytes){Kryo k=new Kryo();k.setRegistrationRequired(false);return k.readClassAndObject(new Input(bytes));}
    static void roundtrip(){
        c.a.SR=(best.f)deserialize(serialize(c.a.SR));c.a.SR.G();
        check(state() instanceof Intercontinental,"Extensão persiste no save");
    }
    static void world()throws Exception{
        Intercontinental root=fixture(2025);
        for(ah club:clubs()){
            best.al coach=new best.al();field(coach,"dm","Técnico "+club.lk());field(coach,"nU",c.a.SR.L().size());
            coach.n(club);c.a.SR.L().add(coach);club.h(coach);
        }
        check(root.worldDates.length==7,"Sete datas reservadas");check(root.annualDates.length==5,"Cinco datas anuais reservadas");
        for(int n=1;n<7;n++)check(root.worldDates[n]-root.worldDates[n-1]>=3,"Descanso entre rodadas do Mundial");
        root.world.start(root);
        for(ah club:root.world.entrants)check(club.kb()==2000000,"Participação do Mundial creditada a cada classificado");
        root.world.start(root);loaded();
        for(ah club:root.world.entrants)check(club.kb()==2000000,"Inicialização e carregamento não repetem participação");
        check(root.world.qualified.size()==32,"32 classificados");
        HashSet<Integer> ids=new HashSet<>();int[] confeds=new int[6];
        for(Qualified entry:root.world.qualified){check(ids.add(entry.club.lk()),"Clube único");confeds[entry.club.gg()]++;}
        check(confeds[0]==12&&confeds[1]==6&&confeds[2]==4&&confeds[3]==4&&confeds[5]==1,"Cotas continentais");
        check(confeds[4]==5,"Concacaf mais vaga anfitriã");
        for(String seed:Rules.SEED_REFS)check(root.world.qualified.stream().anyMatch(q->seed.equals(q.club.jY())),"Seed presente: "+seed);
        check(root.world.groupPhase.yQ().size()==8,"Oito grupos nativos");
        int fixtures=0;for(Object date:root.world.groupPhase.yF())for(Object match:day((Integer)date).h())if(((I)match).ht()==root.world.groupPhase)fixtures++;
        check(fixtures==48,"48 jogos de grupos criados no calendário original");
        for(Object group:root.world.groupPhase.yQ()){
            List<ah> members=((best.G)group).gR();check(members.size()==4,"Quatro clubes no grupo");
            Set<Integer> countries=new HashSet<>();int[] counts=new int[6];
            for(ah club:members){check(countries.add(club.getPais()),"Países separados no sorteio");counts[club.gg()]++;}
            for(int c=0;c<6;c++)check(counts[c]<=(c==0?2:1),"Restrição continental do sorteio");
        }
        int first=root.worldDates[0];int third=root.worldDates[2];
        for(int n=0;n<=third;n++){
            playDay(n);
            if(n==first){roundtrip();root=state();check(root.world.groupPhase.played.size()==16,"Save durante grupos preserva 16 jogos");}
        }
        check(root.world.rounds.size()==1,"Oitavas agendadas após os grupos");
        check(((f.G)root.world.rounds.get(0).zp().get(0)).zW().size()==8,"Oito partidas nas oitavas");
        for(int n=third+1;n<=root.worldDates[6];n++){
            if(n==root.worldDates[3])for(ah club:root.world.entrants)club.k(true);
            playDay(n);
            if(n==root.worldDates[3]){
                for(ah club:root.world.entrants)club.k(false);
                roundtrip();root=state();
            }
        }
        check(root.world.rounds.size()==4,"Quatro fases eliminatórias");
        int knockout=0;for(Stage s:root.world.rounds)knockout+=((f.G)s.zp().get(0)).zW().size();
        check(knockout==15,"15 eliminatórias sem terceiro lugar");
        check(root.world.cS()!=null,"Campeão reconhecido pelo histórico nativo");
        check(root.world.cS().kb()==18000000,"Campeão recebe entrada e quatro avanços sem prêmio nativo duplicado");
        check(root.world.cS().la()[5]==25,"Mundial vale 25 pontos por colocação no ranking nativo");
        check(root.world.cS().ka()!=null&&root.world.cS().ka().lI()>=40,"Título e partidas do Mundial pontuam no ranking do técnico");
        check(root.world.cS().kL().eF()==2000000,"Prêmio de avanço aparece em premiações no balanço do clube humano");
        for(Object group:root.world.groupPhase.yQ()){
            ah eliminated=(ah)((best.G)group).gR().get(3);
            check(eliminated.la()[5]==1,"Eliminado nos grupos também pontua por participação oficial");
        }
        check(root.world.cS().cT().size()>0,"Conquista registrada no clube");
        int totalRanking=root.world.cS().lb(),coachRanking=root.world.cS().ka().lI();long totalCash=root.world.cS().kb();
        int trophies=root.world.cS().cT().size();root.world.mr();root.world.rounds.get(3).pO();
        check(root.world.cS().lb()==totalRanking&&root.world.cS().ka().lI()==coachRanking&&root.world.cS().kb()==totalCash&&root.world.cS().cT().size()==trophies,
                "Repetir finalização não duplica dinheiro, rankings ou troféus");
        roundtrip();root=state();check(root.world.cS()!=null,"Campeão preservado no save");
        JPanel panel=new JPanel();panel.add(new JLabel("original"));
        check(panel(panel,root.world),"Tela integrada do Mundial");
        check(panel.getComponentCount()==2,"Cabeçalho e abas renderizados");
        check(!panel(panel,new f.v()),"Retorno a uma competição original");
        check(panel.getComponentCount()==1,"Componentes originais restaurados");
        a.al actualPanel=new a.al(null,5,root.world);
        check(((JScrollPane)CompetitionMod.member(actualPanel,"ut")).getViewport().getView() instanceof JPanel,
                "Dispatcher da tela original utiliza as abas do patch");
        check(((JComponent)CompetitionMod.member(actualPanel,"vd")).isVisible(),"Navegação original continua visível");
        JPanel content=(JPanel)((JScrollPane)CompetitionMod.member(actualPanel,"ut")).getViewport().getView();
        JTabbedPane tabs=null;for(Component component:content.getComponents())if(component instanceof JTabbedPane)tabs=(JTabbedPane)component;
        check(tabs!=null&&tabs.getTitleAt(tabs.getTabCount()-1).equals("Premiações e ranking"),"Valores e pontuação podem ser consultados dentro da competição");
        tabs.setSelectedIndex(tabs.getTabCount()-1);render(actualPanel,"mod-rewards-panel.png",1100,760);
        a.de trophyRoom=new a.de(null,root.world.cS());
        check(Integer.valueOf(1).equals(trophyRoom.getClientProperty("mod.trophyGroups")),"Sala de troféus mostra o Mundial separadamente");
        System.out.println("OK: Mundial 2025 — 32 clubes, 63 jogos, campeão, telas e saves intermediários.");
    }
    static void annual(int year)throws Exception{
        Intercontinental root=fixture(year);
        // The original initialization event runs after the last continental final.
        for(int n=0;n<root.annualDates[0];n++)day(n).a(true);
        c.a.SR.l(root.annualDates[0]-1);root.zC();
        check(root.rounds.size()==1,"Primeiro play-off criado");
        I opening=(I)((f.G)root.rounds.get(0).zp().get(0)).zW().get(0);
        check(opening.hc().gg()==(year%2==0?3:2),"Alternância AFC/CAF: "+year);
        for(int n=root.annualDates[0];n<=root.annualDates[4];n++){
            playDay(n);
            if(n==root.annualDates[1]){roundtrip();root=state();}
        }
        check(root.rounds.size()==5,"Cinco partidas intercontinentais");
        check(root.afro.cS()!=null&&root.americas.cS()!=null&&root.challenger.cS()!=null&&root.cS()!=null,"Quatro títulos registrados");
        check(root.cS().gg()==0,"Campeão europeu entrou somente na final");
        check(root.cS().kb()==7500000,"Entrada e título da Intercontinental creditados uma única vez");
        check(root.cS().la()[5]==10,"Título Intercontinental vale os 10 pontos nativos do Mundial anual");
        check(root.afro.cS()==root.challenger.cS(),"Clube pode conquistar mais de uma taça");
        check(root.afro.cS().la()[5]==11,"Pontos de duas taças intermediárias e vice Intercontinental acumulam sem repetir bônus nativo");
        a.de trophyRoom=new a.de(null,root.challenger.cS());
        check(Integer.valueOf(2).equals(trophyRoom.getClientProperty("mod.trophyGroups")),"Duas taças distintas na sala de troféus do clube");
        a.di nativeGallery=new a.di(null,root.challenger.cS());
        check(Integer.valueOf(2).equals(nativeGallery.getClientProperty("mod.trophyGroups")),"Galeria realmente aberta por best.S.v distingue as duas taças do mesmo ano");
        check(nativeGallery.pG()==2,"Janela nativa mantém contagem e altura originais para duas taças");
        check(nativeGallery.getLayout() instanceof components.b,"Galeria mantém o layout original do jogo");
        check(((components.k[])CompetitionMod.member(nativeGallery,"ER"))[0].getParent()==nativeGallery,"Taças usam os cartões nativos no painel original");
        check(labels(nativeGallery).contains("Copa África–Ásia–Pacífico")&&labels(nativeGallery).contains("Challenger Cup"),"Galeria mostra os nomes das duas competições");
        check(labels(nativeGallery).contains(Integer.toString(year)),"Galeria mostra o ano da conquista");
        ah doubleWinner=root.challenger.cS();int before=doubleWinner.cT().size();
        at afroCup=root.afro;doubleWinner.cT().removeIf(t->((best.ao)t).gS()==afroCup);
        loaded();check(doubleWinner.cT().size()==before,"Save beta recupera título do clube a partir do campeão registrado");
        loaded();check(doubleWinner.cT().size()==before,"Reconciliação é idempotente e não duplica conquistas");
        long bank=doubleWinner.kb();int ranking=doubleWinner.lb();
        loaded();check(doubleWinner.kb()==bank&&doubleWinner.lb()==ranking,"Reparo da galeria não repete dinheiro nem ranking");
        if(year==2024)render(trophyRoom,"mod-trophies-panel.png",960,500);
        if(year==2024)render(nativeGallery,"mod-club-gallery.png",886,399);
        roundtrip();root=state();check(root.challenger.cS()!=null,"Taças intermediárias persistem");
        System.out.println("OK: Intercontinental "+year+" — chaveamento, quatro taças e save após a segunda partida.");
    }
    static void qualifications()throws Exception{
        Intercontinental root=fixture(2029);ArrayList<Qualified> qualified=root.world.select(root,2029);
        check(qualified.size()==32,"32 classificados em 2029");
        check(qualified.stream().noneMatch(q->q.reason.contains("2021")),"Seeds históricos não reaparecem em 2029");
        // 2029 must ignore even enormous 2029 scores when selecting the 2025–2028 cycle.
        List<Integer> before=new ArrayList<>();for(Qualified q:qualified)before.add(q.club.lk());
        SeasonScore future=new SeasonScore();future.year=2029;future.confed=0;future.clubId=clubs().get(2).lk();future.wins=10000;root.history.add(future);
        List<Integer> after=new ArrayList<>();for(Qualified q:root.world.select(root,2029))after.add(q.club.lk());
        check(before.equals(after),"Ano da edição não afeta seus classificados");
        check(!root.world.isEdition(2026)&&root.world.isEdition(2033),"Periodicidade quadrienal");
        root.world.inactive(2022);check(root.world.status.contains("2025"),"Carreira de 2022 anuncia a primeira edição em 2025");
        System.out.println("OK: classificação de 2029 sem seeds e sem vazamento de resultados futuros.");
    }
    static void legacySave(Path file)throws Exception{
        new best.M(true);
        Kryo k=new Kryo();k.setRegistrationRequired(false);
        try(Input input=new Input(Files.newInputStream(file))){c.a.SR=(best.f)k.readClassAndObject(input);c.a.afQ=(best.ay)k.readClassAndObject(input);}
        c.a.SR.G();
        Intercontinental root=state();
        check(root!=null,"Save original atualizado em memória");
        root.captureSeason();
        check(root.history.size()>0,"Histórico continental recuperado do save original");
        ArrayList<Qualified> firstEdition=root.world.select(root,2025);
        check(firstEdition.size()==32,"Histórico real do save preenche o Mundial de 2025");
        int originalClubs=c.a.SR.P().size();roundtrip();
        check(c.a.SR.P().size()==originalClubs,"Migração conserva os clubes do save");
        root=state();c.a.SR.bs=false;
        // In-memory replay only: exercise the original CPU simulator with real squads.
        c.a.SR.k(2025-c.a.SR.op()+1);c.a.SR.R().clear();root.calendarYear=0;
        c.a.SR.r(2025);best.a.g();
        for(ah club:clubs()){club.k(false);club.I(false);}
        root.world.start(root);c.a.SR.l(root.worldDates[0]);
        best.ah.kh();I game=(I)day(root.worldDates[0]).h().get(0);
        check(game.ev()!=null,"Novo jogo do Mundial já tem estádio na agenda");
        field(game,"dH",null);game.V(); // Existing beta fixtures may have saved a null stadium.
        check(game.ev()!=null&&game.hU()>0&&game.hN()>0,"Motor original calcula público e renda após recuperar estádio do save beta");
        check(game.hl().size()==11&&game.hm().size()==11,"Motor original escalou dois times reais");
        check(game.hu()>=0&&game.hw()>=0,"Motor original simulou placar do Mundial");
        int[] homeRanking=game.hc().ld().clone(),awayRanking=game.hd().ld().clone();
        game.a(true);game.hg();game.hi();game.hh();
        game.hc().k(true);long balance=game.hc().kb();int gates=game.hc().kL().eE();
        game.hf();check(game.hc().kb()==balance+game.hN(),"Renda de estádio creditada ao mandante humano");
        check(game.hc().kL().eE()==gates+game.hN(),"Bilheteria entra na rubrica de renda, separada das premiações");
        game.hf();check(game.hc().kb()==balance+game.hN(),"Pós-jogo repetido não duplica bilheteria");
        int homeId=game.hc().lk();int matchDate=root.worldDates[0];long paid=game.hc().kb();
        roundtrip();root=state();game=(I)day(matchDate).h().get(0);game.hf();
        check(c.a.SR.x(homeId).kb()==paid,"Recibo de bilheteria persiste no save");
        game.hc().k(false);
        c.a.SR.h(false);
        a.hk results=new a.hk(null);results.i(game);
        check(!"n/i".equals(((JLabel)CompetitionMod.member(results,"Dy")).getText()),"Tela nativa de resultado mostra o público do Mundial");
        check(!"n/i".equals(((JLabel)CompetitionMod.member(results,"Dz")).getText()),"Tela nativa de resultado mostra a renda do Mundial");
        render(results,"mod-match-income.png",1000,740);
        check(game.hc().ld()[0]-homeRanking[0]==(game.hu()>game.hw()?3:game.hu()==game.hw()?1:0),"Partida real pontua no ranking corrido do mandante");
        check(game.hd().ld()[0]-awayRanking[0]==(game.hw()>game.hu()?3:game.hu()==game.hw()?1:0),"Partida real pontua no ranking corrido do visitante");
        check(game.hc().a(root.world,root.world.el()).w()>0,"Estatísticas nativas atualizadas após simulação real");
        render(new a.al(null,5,root.world),"mod-world-panel.png",1100,760);
        rankingPanel(game.hc(),1,"mod-ranking-points.png");
        System.out.println("OK: motor original simulou uma partida do Mundial com elencos reais.");
        System.out.println("OK: save original lido e convertido apenas em memória; "+originalClubs+" clubes.");
    }
    static void legacyTitles()throws Exception{
        Intercontinental root=fixture(2023);ah champion=clubs().get(0);
        winner(root,1,champion);champion.q(root);
        c.a.SR.k(3);c.a.SR.R().clear();c.a.SR.r(2024);best.a.g();
        check(root.mn().isEmpty()&&root.legacy.mn().size()==1,"Título antigo arquivado sem virar Intercontinental");
        check(((best.ao)champion.cT().get(0)).gS()==root.legacy,"Conquista antiga aponta para o torneio original");
        root.legacy.setNome("Mundial anual (formato original)");roundtrip();root=state();
        check(root.legacy.getNome().equals("Mundial de Clubes (até 2023)"),"Nome corrigido também em saves beta existentes");
        check(root.legacy.mA()[0].equals("tr_mundial"),"Mundial anterior usa a taça original");
        check(root.legacy.es()!=null&&root.legacy.es().getIconHeight()==20,"Ícone do Mundial anterior cabe no seletor");
        System.out.println("OK: títulos anteriores a 2024 preservados no torneio original.");
    }
    static void tiebreakers()throws Exception{
        Intercontinental root=fixture(2025);root.world.start(root);Groups group=root.world.groupPhase;
        ArrayList<ah> members=new ArrayList<>(((best.G)group.yQ().get(0)).gR());
        ah a=members.get(0),b=members.get(1),c=members.get(2),d=members.get(3);
        group.played.clear();
        ah[][] pairs={{a,b},{b,c},{c,a},{a,d},{b,d},{c,d}};
        int[][] scores={{2,0},{2,0},{3,1},{10,0},{1,0},{1,0}};
        for(int n=0;n<pairs.length;n++){
            I game=new I();game.s(pairs[n][0]);game.t(pairs[n][1]);score(game,scores[n][0],scores[n][1]);group.played.add(game);
        }
        group.sortGroup(members);
        check(members.get(0)==c&&members.get(1)==a&&members.get(2)==b,"Desempate reaplicado entre clubes que permanecem empatados");
        best.A yellow=new best.A(0);yellow.a(2);yellow.k(a);yellow.f(new best.F());group.played.get(0).hE().add(yellow);
        check(group.fairPlay(a)==-1,"Cartão amarelo vale -1 no fair play");
        best.A secondYellow=new best.A(0);secondYellow.a(3);secondYellow.k(a);secondYellow.f(yellow.eo());group.played.get(0).hE().add(secondYellow);
        check(group.fairPlay(a)==-3,"Segundo amarelo conta -3, sem duplicar o primeiro");
        SeasonScore european=new SeasonScore();european.confed=0;european.wins=3;european.draws=2;european.mainMatches=10;european.groupStage=true;european.knockoutRounds=2;
        check(european.points()==18,"Pesos de pontos UEFA e bônus por fases");
        european.confed=1;check(european.points()==20,"Pesos de pontos das outras confederações");
        System.out.println("OK: confronto direto reaplicado, fair play e pontuação continental.");
    }
    static void knockoutAndSeasonHooks()throws Exception{
        Intercontinental root=fixture(2024);
        for(int n=0;n<root.annualDates[0];n++)day(n).a(true);
        c.a.SR.l(root.annualDates[0]-1);root.zC();Stage opening=root.rounds.get(0);
        I match=(I)((f.G)opening.zp().get(0)).zW().get(0);ah penaltyWinner=match.hd();
        score(match,0,0);field(match,"gf",true);match.u(penaltyWinner);
        c.a.SR.l(root.annualDates[0]);day(root.annualDates[0]).a(true);opening.pO();
        check(((f.G)opening.zp().get(0)).zY().get(0)==penaltyWinner,"Desempate por pênaltis respeita o vencedor do motor");
        for(int n=root.annualDates[0]+1;n<=root.annualDates[4];n++)playDay(n);
        java.lang.reflect.Method advance=best.f.class.getDeclaredMethod("ah");advance.setAccessible(true);advance.invoke(c.a.SR);
        check(year()==2025&&root.calendarYear==2025,"Hook nativo da virada cria calendário de 2025");
        CompetitionMod.seasonReady();
        check(root.world.startedYear==2025&&root.world.groupPhase!=null,"Inicialização da temporada aciona o Mundial automaticamente");
        check(root.cv(3)!=null,"Campeão intercontinental de 2024 preservado após virada");
        System.out.println("OK: pênaltis nativos e virada 2024 → 2025 com criação automática do Mundial.");
    }
    public static void main(String[] args)throws Exception{
        assets();world();annual(2024);annual(2025);qualifications();legacyTitles();tiebreakers();knockoutAndSeasonHooks();galleryFamilies();rewardMigration();stadiumCases();
        if(args.length>0)legacySave(Paths.get(args[0]));
        System.out.println("PASS: "+checks+" verificações de integração.");
    }
    static String labels(Component component){
        StringBuilder text=new StringBuilder();if(component instanceof JLabel)text.append(((JLabel)component).getText());
        if(component instanceof Container)for(Component child:((Container)component).getComponents())text.append(labels(child));
        return text.toString();
    }
    static void assets(){
        new best.M(true);
        try{
            java.lang.reflect.Method prize=f.G.class.getDeclaredMethod("c",int.class,int.class,boolean.class);prize.setAccessible(true);
            check(((Integer)prize.invoke(new f.G(),5,0,false))==2000000,"Premiação original de fases fora do patch permanece inalterada");
        }catch(ReflectiveOperationException error){throw new AssertionError(error);}
        for(String id:new String[]{"club_world_cup","intercontinental_cup","africa_asia_pacific","derby_of_americas","challenger_cup"}){
            for(int size:new int[]{20,128}){
                ImageIcon icon=icon(id,size);check(icon!=null&&icon.getIconWidth()==size&&icon.getIconHeight()==size,"Ícone padronizado: "+id+" "+size);
                BufferedImage image=(BufferedImage)icon.getImage();
                check((image.getRGB(0,0)>>>24)==0,"Canto transparente: "+id+" "+size);
                int opaque=0;for(int y=0;y<size;y++)for(int x=0;x<size;x++)if((image.getRGB(x,y)>>>24)>128)opaque++;
                check(opaque>size*size/30&&opaque<size*size*0.85,"Silhueta visível e fundo transparente: "+id+" "+size);
            }
            ImageIcon nativeAsset=best.aq.x("tr_mod_"+id);
            check(nativeAsset!=null&&nativeAsset.getIconHeight()>100,"API original encontra a taça: "+id);
        }
    }
    static void galleryFamilies()throws Exception{
        Intercontinental root=fixture(2025);ah champion=clubs().get(0);
        for(at cup:new at[]{root.world,root,root.afro,root.americas,root.challenger})winner(cup,4,champion);
        winner(root.legacy,1,champion);winner(root.afro,3,champion);
        loaded();check(champion.cT().size()==7,"Reconcilia cinco famílias novas, o Mundial antigo e outra edição");
        roundtrip();root=state();champion=c.a.SR.x(champion.lk());
        a.di gallery=new a.di(null,champion);
        check(Integer.valueOf(6).equals(gallery.getClientProperty("mod.trophyGroups")),"Seis famílias separadas após carregar save");
        check(gallery.pG()==6,"Janela nativa dimensiona seis cartões em duas fileiras");
        String names=labels(gallery);
        for(at cup:new at[]{root.world,root,root.afro,root.americas,root.challenger,root.legacy})
            check(names.contains(cup.getNome()),"Galeria exibe "+cup.getNome());
        check(names.contains("2025, 2024"),"Duas edições de África–Ásia–Pacífico contabilizadas");
        render(gallery,"mod-all-trophies.png",886,399);
        rankingPanel(champion,2,"mod-ranking-competitions.png");
        System.out.println("OK: galeria nativa com cinco taças novas, Mundial antigo e múltiplas edições.");
    }
    static void rankingPanel(ah club,int mode,String filename)throws Exception{
        a.hc panel=new a.hc(null,club);
        ((JComboBox)CompetitionMod.member(panel,"KK")).setSelectedIndex(mode);
        field(panel,"uk",club);field(panel,"w",mode);
        java.lang.reflect.Method refresh=a.hc.class.getDeclaredMethod("rW");refresh.setAccessible(true);refresh.invoke(panel);
        ArrayList<components.ad> rows=(ArrayList<components.ad>)CompetitionMod.member(panel,"Fj");
        int sum=0,named=0;for(components.ad row:rows){sum+=row.uy();if(row instanceof NativeUi.RankRow)named++;}
        check(named>0,"Ranking original discrimina as competições novas");
        check(sum==(mode==2?club.lb():club.ld()[0]),"Soma do detalhamento coincide com total do ranking original");
        JTable table=(JTable)CompetitionMod.member(panel,"Fl");
        check(table.getModel() instanceof b.I,"Detalhamento mantém modelo nativo do ranking");
        for(int row=0;row<table.getRowCount();row++){
            Object value=table.getValueAt(row,0);
            if(value instanceof NativeUi.RankRow){
                Component rendered=table.prepareRenderer(table.getCellRenderer(row,0),row,0);
                check(rendered instanceof JLabel&&((JLabel)rendered).getText().equals(((NativeUi.RankRow)value).name),"Renderer original exibe o nome de cada nova competição");
            }
        }
        // Offline replay does not initialize domestic leagues used by the flag renderer.
        ((JComboBox)CompetitionMod.member(panel,"ue")).setRenderer(new DefaultListCellRenderer());
        render(panel,filename,826,602);
    }
    static void rewardMigration()throws Exception{
        Intercontinental root=fixture(2025);ah champion=clubs().get(0);
        best.al coach=new best.al();field(coach,"dm","Técnico legado");field(coach,"nU",0);coach.n(champion);c.a.SR.L().add(coach);champion.h(coach);
        // Reproduce the released beta's native 10/25-point placement effects.
        champion.a(root.afro,1,0);winner(root.afro,4,champion);
        field(root.afro.mo(),"bU",coach.lT());
        check(champion.lb()==10&&coach.lI()==25,"Baseline do beta usa bônus nativos do Mundial anual");
        roundtrip();root=state();champion=c.a.SR.x(champion.lk());coach=champion.ka();
        check(champion.lb()==3&&coach.lI()==10,"Migração substitui bônus antigos pelos pontos da taça, sem somá-los duas vezes");
        check(champion.kb()==0,"Conquista histórica não repete prêmio em dinheiro");
        loaded();check(champion.lb()==3&&coach.lI()==10,"Migração de ranking é idempotente");
        root.world.start(root);
        for(ah club:root.world.entrants)club.e(0);
        root.history.removeIf(s->s.origin!=null&&s.origin.startsWith("reward:v1:")&&s.origin.contains(":70:entry:"));
        roundtrip();root=state();
        for(ah club:root.world.entrants)check(club.kb()==2000000,"Carreira em andamento recupera participação não paga pelo beta");
        loaded();for(ah club:root.world.entrants)check(club.kb()==2000000,"Participação recuperada não se repete");
        System.out.println("OK: saldo, balanço, ranking de clubes/técnicos e recibos persistentes no save do beta.");
    }
    static void stadiumCases()throws Exception{
        Intercontinental root=fixture(2025);ah home=clubs().get(0),away=clubs().get(1);
        home.setDivisao(1);away.setDivisao(1);home.setReputacao(3);away.setReputacao(3);
        home.a(new best.v("Arena do mandante",20000,home));home.k(true);int date=10;
        for(at cup:new at[]{root.world,root,root.afro,root.americas,root.challenger}){
            I match=new I(null,0,home,away,date++,cup,null);
            check(match.ev()==home.ev(),"Estádio associado em "+cup.getNome());
            match.ev().b(match);score(match,2,0);
            check(match.hU()>0&&match.hN()>0,"Público e renda calculados em "+cup.getNome());
            long bank=home.kb();int income=home.kL().eE();match.hf();
            check(home.kb()==bank+match.hN()&&home.kL().eE()==income+match.hN(),"Bilheteria da taça creditada como renda");
            match.hf();check(home.kb()==bank+match.hN(),"Bilheteria da taça não se repete");
        }
        long bank=home.kb();I originalWorld=new I(null,0,home,away,date++,new f.A(),null);
        check(originalWorld.ev()==null,"Mundial original fora do patch preserva tratamento de estádio");
        originalWorld.aW(1000);originalWorld.hf();check(home.kb()==bank,"Política de renda do Mundial original permanece inalterada");
        I originalContinental=new I(null,0,home,away,date,continents()[0],null);originalContinental.aW(12345);originalContinental.hf();
        check(home.kb()==bank+12345,"Crédito de renda de competições originais não sofre duplicação");
        System.out.println("OK: bilheteria das cinco taças e política original preservada fora do patch.");
    }
}
