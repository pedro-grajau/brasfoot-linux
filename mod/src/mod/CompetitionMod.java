// SPDX-License-Identifier: GPL-3.0-only
package mod;

import best.I;
import best.L;
import best.ah;
import best.aj;
import best.at;
import best.i;
import est.LoadLigaOptions;
import java.awt.*;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.text.Normalizer;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** Extension of the original competition objects; the match simulator is unchanged. */
@SuppressWarnings({"unchecked", "rawtypes"})
public final class CompetitionMod {
    private CompetitionMod() {}
    public static int year() { return c.a.SR.op() + c.a.SR.H() - 1; }
    static List<ah> clubs() { return (List<ah>)(List)c.a.SR.P(); }
    static best.a day(int index) { return (best.a)c.a.SR.R().get(index); }
    static String ref(ah club) { return club.jY() == null ? "id:" + club.lk() : club.jY(); }
    static String normalize(String s) {
        return Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }
    static int confederation(ah club) { return club.gg(); }
    static at[] continents() {
        return new at[]{c.a.SR.aI(), c.a.SR.aF(), c.a.SR.aO(),
                c.a.SR.aL(), c.a.SR.aP(), c.a.SR.aQ()};
    }

    /** Called by the patched original getter; changes no fields in best.f. */
    public static f.A annual(f.A original) {
        if (original == null || original instanceof Intercontinental) return original;
        Intercontinental replacement = new Intercontinental();
        try {
            for (Class<?> type = f.A.class; type != Object.class; type = type.getSuperclass()) {
                for (Field field : type.getDeclaredFields()) {
                    if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) continue;
                    field.setAccessible(true);
                    field.set(replacement, field.get(original));
                }
            }
        } catch (ReflectiveOperationException e) { throw new IllegalStateException("Não foi possível preservar o Mundial original", e); }
        if(c.a.SR!=null&&year()>=Rules.INTERCONTINENTAL_FIRST_YEAR&&!original.mn().isEmpty()){
            replacement.legacy.mn().addAll(original.mn());
            try{Field titles=at.class.getDeclaredField("tT");titles.setAccessible(true);titles.set(replacement,new ArrayList<>());}
            catch(ReflectiveOperationException e){throw new IllegalStateException("Falha ao arquivar títulos anteriores ao patch",e);}
        }
        c.a.SR.a(replacement);
        return replacement;
    }
    public static Intercontinental state() {
        if (c.a.SR == null || !c.a.SR.isJogaIntClubes()) return null;
        return (Intercontinental)c.a.SR.aR();
    }
    /** Repair existing beta saves from recorded champions, without replaying rewards. */
    public static void loaded() {
        Intercontinental root=state();if(root==null)return;
        ArrayList<at> cups=new ArrayList<>(root.additional());cups.add(root);
        for(at cup:cups)for(Object object:cup.mn()){
            i record=(i)object;ah winner=record.ce();if(winner==null)continue;
            ensureTitle(winner.cT(),cup,record.H(),winner.lk());
            if(record.ci()!=null)ensureTitle(record.ci().cT(),cup,record.H(),winner.lk());
        }
        if(year()>=Rules.INTERCONTINENTAL_FIRST_YEAR){
            for(ah club:clubs())root.archiveTitles(club.cT());
            for(Object coach:c.a.SR.L())root.archiveTitles(((best.al)coach).cT());
        }
        Rewards.reconcile(root);
    }
    /** Custom stages credit their prize once, including CPU clubs, through Rewards. */
    public static int nativePrize(int original,f.G round){return round.zT() instanceof Stage?0:original;}
    static boolean customMatch(I match){
        return match.hy() instanceof ViewCup&&Rewards.id(match.hy())!=null
                &&(!(match.hy() instanceof Intercontinental)||year()>=Rules.INTERCONTINENTAL_FIRST_YEAR);
    }
    public static void prepareMatch(I match){
        if(!customMatch(match)||match.ev()!=null||match.hc().ev()==null)return;
        try{
            Field stadium=I.class.getDeclaredField("dH");stadium.setAccessible(true);stadium.set(match,match.hc().ev());
            match.O(match.ev().dX());
        }catch(ReflectiveOperationException error){throw new IllegalStateException("Estádio da partida incompatível",error);}
    }
    public static void stadiumRevenue(I match){if(customMatch(match))Rewards.gate(match);}
    public static void rankingDetails(JPanel panel){NativeUi.ranking(panel);}
    public static Component rankingCell(Component rendered,Object value,int column){return NativeUi.rankCell(rendered,value,column);}
    /** Build-time export for native HTML renderers, which do not resize image files. */
    public static void exportNativeIcons(String directory)throws java.io.IOException{
        for(String id:new String[]{"club_world_cup","intercontinental_cup","africa_asia_pacific","derby_of_americas","challenger_cup"}){
            ImageIcon image=NativeUi.image(new Cup(id,id,70));
            java.nio.file.Path path=java.nio.file.Paths.get(directory,"tr_mod_"+id+".png");
            java.nio.file.Files.createDirectories(path.getParent());
            javax.imageio.ImageIO.write((java.awt.image.BufferedImage)image.getImage(),"png",path.toFile());
        }
    }
    static void ensureTitle(ArrayList records,at cup,int season,int clubId){
        for(Object object:records){best.ao title=(best.ao)object;
            if(title.H()==season&&title.b()==cup.b()&&title.el()==cup.gg()&&title.ct()==clubId){
                title.m(cup);return;
            }
        }
        best.ao title=new best.ao();title.k(season);title.C(clubId);title.a(cup.b());title.R(cup.gg());title.m(cup);records.add(title);
    }
    /** Capture completed results before the original calendar is cleared. */
    public static void seasonEnd() {
        Intercontinental root = state();
        if (root != null) root.captureSeason();
    }
    /** Reserve unoccupied dates before any leagues build their fixtures. */
    public static void calendar() {
        Intercontinental root = state();
        if (root == null) return;
        root.prepareCalendar();
    }
    public static void seasonReady() {
        Intercontinental root=state();
        if(root!=null&&root.world.isEdition(year()))root.world.start(root);
    }
    public static at[] allCompetitions(at[] original) {
        Intercontinental root = state();
        if (root == null) return original;
        ArrayList<at> result = new ArrayList<>(Arrays.asList(original));
        for (at cup : root.additional()) if (!result.contains(cup)) result.add(cup);
        return result.toArray(new at[0]);
    }
    public static ArrayList list(ArrayList original, int category, boolean include) {
        Intercontinental root = state();
        if (root != null && category == 1) {
            if(year()>=Rules.INTERCONTINENTAL_FIRST_YEAR&&!original.contains(root))original.add(0,root);
            for (at cup : root.additional()) if (!original.contains(cup)) original.add(cup);
        }
        return original;
    }
    static int[] reserve(List<Integer> candidates, int count, int gap) {
        ArrayList<Integer> selected = new ArrayList<>();
        for (int index : candidates) {
            if (selected.isEmpty() || index - selected.get(selected.size()-1) >= gap) {
                selected.add(index);
                if (selected.size() == count) return selected.stream().mapToInt(Integer::intValue).toArray();
            }
        }
        throw new IllegalStateException("Calendário sem " + count + " datas livres com intervalo de " + gap + " dias");
    }
    static List<Integer> dates(int firstMonth, int firstDay, int lastMonth, int lastDay, boolean annual) {
        ArrayList<Integer> result = new ArrayList<>();
        int lastContinental = -1;
        if (annual) lastContinental = best.a.d(4);
        for (int n = 0; n < c.a.SR.R().size(); n++) {
            best.a d = day(n);
            int stamp = (d.c()+1)*100 + d.a().get(Calendar.DAY_OF_MONTH);
            if (stamp < firstMonth*100+firstDay || stamp > lastMonth*100+lastDay) continue;
            if (annual && n <= lastContinental) continue;
            if (d.b() == 0 || annual && d.b() == 5) result.add(n);
        }
        return result;
    }
    static void writeDates(int[] dates) { for (int index : dates) day(index).a(5); }
    static final Map<String,ImageIcon> ICONS=new HashMap<>();
    static ImageIcon icon(String id, int height) {
        if(id==null)return null;
        String key=id+":"+height;if(ICONS.containsKey(key))return ICONS.get(key);
        String file = id+".png";
        java.net.URL url = CompetitionMod.class.getResource("/mod/trophies/" + file);
        if (url == null) return null;
        try{
            java.awt.image.BufferedImage full=javax.imageio.ImageIO.read(url);
            int x0=full.getWidth(),y0=full.getHeight(),x1=-1,y1=-1;
            for(int y=0;y<full.getHeight();y++)for(int x=0;x<full.getWidth();x++)if((full.getRGB(x,y)>>>24)>16){
                x0=Math.min(x0,x);x1=Math.max(x1,x);y0=Math.min(y0,y);y1=Math.max(y1,y);
            }
            if(x1<0)return null;
            java.awt.image.BufferedImage cropped=full.getSubimage(x0,y0,x1-x0+1,y1-y0+1);
            int inset=height<=24?1:4,available=height-2*inset;
            double scale=Math.min((double)available/cropped.getWidth(),(double)available/cropped.getHeight());
            int width=Math.max(1,(int)Math.round(cropped.getWidth()*scale)),h=Math.max(1,(int)Math.round(cropped.getHeight()*scale));
            java.awt.image.BufferedImage canvas=new java.awt.image.BufferedImage(height,height,java.awt.image.BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics=canvas.createGraphics();graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.drawImage(cropped,(height-width)/2,(height-h)/2,width,h,null);graphics.dispose();
            ImageIcon result=new ImageIcon(canvas);ICONS.put(key,result);return result;
        }catch(java.io.IOException error){throw new IllegalStateException("Imagem de troféu inválida: "+id,error);}
    }
    static void place(at cup, List<Stage> stages, List<ah> others) {
        ArrayList<ah> order = new ArrayList<>();
        for (int r = stages.size()-1; r >= 0; r--) {
            Stage stage = stages.get(r);
            if (!stage.finished) continue;
            for (Object obj : stage.zp()) {
                f.G round = (f.G)obj;
                for (Object match : round.zW()) {
                    I game = (I)match;
                    ah winner = round.zY().contains(game.hc()) ? game.hc() : game.hd();
                    ah loser = winner == game.hc() ? game.hd() : game.hc();
                    if (!order.contains(winner)) order.add(winner);
                    if (!order.contains(loser)) order.add(loser);
                }
            }
        }
        for (ah club : others) if (!order.contains(club)) order.add(club);
        for (int n = 0; n < order.size(); n++) Rewards.placing(cup,order.get(n),n+1);
    }

    public interface ViewCup {
        List<Stage> stages();
        String trophy();
        default Groups groups() { return null; }
    }
    interface StageHost { void completed(Stage stage, ArrayList<ah> winners); }

    public static class Cup extends at implements ViewCup, Serializable {
        private static final long serialVersionUID = 1L;
        public ArrayList<Stage> rounds = new ArrayList<>();
        public ArrayList<ah> entrants = new ArrayList<>();
        public String trophyId;
        public int editionYear;
        public Cup() {}
        Cup(String name, String trophy, int identity) {
            setNome(name); trophyId=trophy; F(5,identity); O(false);
        }
        public List<Stage> stages() { return rounds; }
        public String trophy() { return trophyId; }
        @Override public String getNome() { return trophyId==null&&el()==7?"Mundial de Clubes (até 2023)":super.getNome(); }
        @Override public ImageIcon es() {
            if(trophyId!=null)return icon(trophyId,20);
            ImageIcon original=best.aq.x("tr_mundial");
            return original==null?null:new ImageIcon(original.getImage().getScaledInstance(-1,20,Image.SCALE_SMOOTH));
        }
        @Override public String[] mA() { return new String[]{trophyId==null?"tr_mundial":"tr_mod_"+trophyId, getNome()}; }
        @Override public L[] mB() { return rounds.toArray(new L[0]); }
        @Override public String[] b(L phase) { return new String[]{phase.io()}; }
        @Override public void mr() { place(this, rounds, entrants); }
        void reset(int year) { rounds.clear(); entrants.clear(); editionYear=year; mw(); }
        void award(Stage stage, ah winner, ah runnerUp) {
            if (cS() == null) new i(this,stage,winner,runnerUp);
        }
    }

    /** A single native knockout round, with a callback instead of automatic pairing. */
    public static class Stage extends f.z implements Serializable {
        private static final long serialVersionUID = 1L;
        public StageHost host;
        public String label;
        public int tag;
        public boolean finished;
        public int seasonYear;
        public Stage() {}
        Stage(StageHost host, at owner, String label, int tag, List<ah> clubs) {
            super(null,clubs.size(),5,1,new boolean[7],owner,-1);
            this.host=host; this.label=label; this.tag=tag; this.seasonYear=year();
            setNome(owner.getNome());
            f.G round = new f.G();
            round.a(this,new ArrayList<>(clubs),0,false,0,0,5,false);
        }
        @Override public String io() { return label; }
        @Override public void pO() {
            if (finished) return;
            super.pO();
            finished=true;
            ArrayList<ah> winners=new ArrayList<ah>(((f.G)zp().get(0)).zY());
            Rewards.stage(this,winners);
            host.completed(this,winners);
        }
    }

    /** Stored in the original best.f.aA field, preserving the other save schemas. */
    public static class Intercontinental extends f.A implements ViewCup, StageHost, Serializable {
        private static final long serialVersionUID = 1L;
        public WorldCup world = new WorldCup();
        public Cup afro = new Cup("Copa África–Ásia–Pacífico","africa_asia_pacific",72);
        public Cup americas = new Cup("Derby das Américas","derby_of_americas",73);
        public Cup challenger = new Cup("Challenger Cup","challenger_cup",74);
        public Cup legacy = new Cup("Mundial de Clubes (até 2023)",null,7);
        public ArrayList<Stage> rounds = new ArrayList<>();
        public ArrayList<SeasonScore> history = new ArrayList<>();
        public int calendarYear;
        public int initializedYear;
        public int[] annualDates = new int[0];
        public int[] worldDates = new int[0];
        public ah[] champions;
        public ah pacificWinner;
        public ah americasWinner;
        public String diagnostic="";
        public Intercontinental() { super(); }
        public List<at> additional() {
            ArrayList<at> cups=new ArrayList<>(Arrays.asList(world,afro,americas,challenger));
            if(!legacy.mn().isEmpty())cups.add(legacy);
            return cups;
        }
        public List<Stage> stages() { return rounds; }
        public String trophy() { return "intercontinental_cup"; }
        @Override public String getNome() {
            return c.a.SR != null && year() >= Rules.INTERCONTINENTAL_FIRST_YEAR
                    ? "Copa Intercontinental de Clubes" : super.getNome();
        }
        @Override public int el() { return year() >= Rules.INTERCONTINENTAL_FIRST_YEAR ? 71 : super.el(); }
        @Override public int gg() { return el(); }
        @Override public boolean mq() { return year() < Rules.INTERCONTINENTAL_FIRST_YEAR && super.mq(); }
        @Override public ImageIcon es() { return year() >= Rules.INTERCONTINENTAL_FIRST_YEAR ? icon(trophy(),20) : super.es(); }
        @Override public String[] mA() {
            return year() >= Rules.INTERCONTINENTAL_FIRST_YEAR
                    ? new String[]{"tr_mod_intercontinental_cup",getNome()} : super.mA();
        }
        @Override public L[] mB() {
            return year() >= Rules.INTERCONTINENTAL_FIRST_YEAR ? rounds.toArray(new L[0]) : super.mB();
        }
        @Override public String[] b(L phase) {
            return phase instanceof Stage ? new String[]{phase.io()} : super.b(phase);
        }
        @Override public void mr() {
            if (year() >= Rules.INTERCONTINENTAL_FIRST_YEAR) place(this,rounds,Collections.emptyList());
            else super.mr();
        }
        public void captureSeason() {
            int currentYear=year();
            // Recover actual aggregated continental records, including older saves.
            for (ah club : clubs()) {
                for (Object object : club.le()) {
                    aj record=(aj)object;
                    int civilYear=c.a.SR.op()+record.H()-1;
                    if (record.b()!=4 || record.el()<0 || record.el()>5 || record.w()==0) continue;
                    SeasonScore score=new SeasonScore();
                    score.year=civilYear; score.confed=record.el(); score.clubId=club.lk();
                    score.wins=record.cm(); score.draws=record.w()-record.cm()-record.co();
                    score.goals=record.ls(); score.against=record.lt();
                    score.placement=record.lu(); score.origin="estatísticas continentais do Brasfoot";
                    SeasonScore retained=findScore(score.year,score.confed,score.clubId);
                    if(retained!=null&&retained.mainMatches>0)continue;
                    putScore(score);
                }
            }
            // Preserve a finer match-based ledger for seasons played with this patch.
            at[] continental=continents();
            HashMap<String,SeasonScore> current=new HashMap<>();
            HashMap<String,HashSet<f.G>> stages=new HashMap<>();
            for(int d=0;d<c.a.SR.R().size();d++) {
                if (!day(d).e()) continue;
                for(Object object:day(d).h()) {
                    I match=(I)object; int conf=-1;
                    for(int n=0;n<continental.length;n++) if(match.hy()==continental[n]) conf=n;
                    if(conf<0)continue;
                    // Exclude qualifying rounds from UEFA's main-competition ledger.
                    if(conf==0 && match.ht() instanceof f.z && ((f.z)match.ht()).zf()>=4000
                            && ((f.z)match.ht()).zf()<=4004)continue;
                    if(conf==1 && match.ht() instanceof f.z && ((f.z)match.ht()).zf()>=4101
                            && ((f.z)match.ht()).zf()<=4103)continue;
                    ah[] pair={match.hc(),match.hd()}; int[] goals={match.hu(),match.hw()};
                    for(int n=0;n<2;n++) {
                        String key=conf+":"+pair[n].lk();
                        SeasonScore score=current.get(key);
                        if(score==null) {
                            score=new SeasonScore(); score.year=currentYear; score.confed=conf;
                            score.clubId=pair[n].lk(); score.origin="partidas continentais da carreira";
                            current.put(key,score);
                        }
                        score.mainMatches++;
                        if(match.ht() instanceof f.s)score.groupStage=true;
                        if(match.ht() instanceof f.z){
                            HashSet<f.G> rounds=stages.computeIfAbsent(key,k->new HashSet<>());
                            for(Object stage:((f.z)match.ht()).zp())if(((f.G)stage).zW().contains(match))rounds.add((f.G)stage);
                            score.knockoutRounds=rounds.size();
                        }
                        if(goals[n]>goals[1-n]) score.wins++;
                        if(goals[n]==goals[1-n]) score.draws++;
                        score.goals+=goals[n]; score.against+=goals[1-n];
                    }
                }
            }
            // Placement records retain advancement for a transparent career coefficient.
            for(SeasonScore score:current.values()) {
                SeasonScore previous=findScore(score.year,score.confed,score.clubId);
                if(previous!=null)score.placement=previous.placement;
                putScore(score);
            }
        }
        SeasonScore findScore(int year,int confed,int id) {
            for(SeasonScore score:history)if(score.year==year&&score.confed==confed&&score.clubId==id)return score;
            return null;
        }
        void putScore(SeasonScore score) {
            SeasonScore previous=findScore(score.year,score.confed,score.clubId);
            if(previous!=null)history.remove(previous);
            history.add(score);
        }
        public void prepareCalendar() {
            if (calendarYear==year()) return;
            calendarYear=year(); rounds.clear(); champions=null; initializedYear=0;
            pacificWinner=null;americasWinner=null;diagnostic="";
            afro.reset(year());americas.reset(year());challenger.reset(year());
            if(year()>=Rules.INTERCONTINENTAL_FIRST_YEAR){
                Iterator<?> titles=mn().iterator();
                while(titles.hasNext()){
                    i record=(i)titles.next();
                    if(c.a.SR.op()+record.H()-1<Rules.INTERCONTINENTAL_FIRST_YEAR){legacy.mn().add(record);titles.remove();}
                }
                for(ah club:clubs())archiveTitles(club.cT());
                for(Object coach:c.a.SR.L())archiveTitles(((best.al)coach).cT());
            }
            if(year()>=Rules.INTERCONTINENTAL_FIRST_YEAR) {
                annualDates=reserve(dates(11,1,12,31,true),5,3);
                // Remove only the old annual-cup slots, which have no fixtures yet.
                for(int n=0;n<c.a.SR.R().size();n++)if(day(n).b()==5)day(n).a(0);
                writeDates(annualDates);
                // The original event starts the cup after the last continental date.
                // Its event is still scheduled by best.f.ae(), after this hook.
            }
            if(world.isEdition(year())) {
                worldDates=reserve(dates(6,14,7,31,false),7,3);
                writeDates(worldDates);
                world.plannedYear=year();
            } else { worldDates=new int[0];world.inactive(year()); }
        }
        void archiveTitles(ArrayList titles){
            for(Object object:titles)if(object instanceof best.ao){
                best.ao title=(best.ao)object;
                if(title.gS()==this&&c.a.SR.op()+title.H()-1<Rules.INTERCONTINENTAL_FIRST_YEAR){title.m(legacy);title.R(7);}
            }
        }
        /** Triggered by the original annual-cup creation event. */
        @Override public void zC() {
            if(year()<Rules.INTERCONTINENTAL_FIRST_YEAR) {super.zC();return;}
            if(initializedYear==year())return;
            champions=new ah[6];at[] source=continents();
            for(int n=0;n<6;n++) {
                champions[n]=source[n]==null?null:source[n].cS();
                if(champions[n]==null)throw new IllegalStateException("Campeão continental ainda indefinido: "+Rules.CONFEDERATIONS[n]);
            }
            initializedYear=year();
            Rewards.entry(this,year(),Arrays.asList(champions));
            boolean afcFirst=(year()-2024)%2==0;
            addStage(afro,"Play-off África–Ásia–Pacífico",0,
                    Arrays.asList(champions[afcFirst?3:2],champions[5]));
        }
        Stage addStage(at owner,String label,int tag,List<ah> entrants) {
            Stage stage=new Stage(this,owner,label,tag,entrants);rounds.add(stage);
            if(owner instanceof Cup) {
                Cup cup=(Cup)owner;cup.rounds.add(stage);
                for(ah club:entrants)if(!cup.entrants.contains(club))cup.entrants.add(club);
            }
            return stage;
        }
        @Override public void completed(Stage stage,ArrayList<ah> winners) {
            ah winner=winners.get(0);
            I game=(I)((f.G)stage.zp().get(0)).zW().get(0);
            ah runner=winner==game.hc()?game.hd():game.hc();
            if(stage.tag==0) {
                boolean afcFirst=(year()-2024)%2==0;
                addStage(afro,"Final África–Ásia–Pacífico",1,Arrays.asList(champions[afcFirst?2:3],winner));
            } else if(stage.tag==1) {
                afro.award(stage,winner,runner);pacificWinner=winner;
                addStage(americas,"Derby das Américas",2,Arrays.asList(champions[1],champions[4]));
            } else if(stage.tag==2) {
                americas.award(stage,winner,runner);americasWinner=winner;
                addStage(challenger,"Challenger Cup",3,Arrays.asList(pacificWinner,winner));
            } else if(stage.tag==3) {
                challenger.award(stage,winner,runner);
                addStage(this,"Final Intercontinental",4,Arrays.asList(champions[0],winner));
            } else if(stage.tag==4 && cS()==null) new i(this,stage,winner,runner);
        }
    }

    public static class SeasonScore implements Serializable {
        private static final long serialVersionUID=1L;
        public int year,confed,clubId,wins,draws,goals,against,placement;
        public int mainMatches,knockoutRounds;
        public boolean groupStage;
        public String origin;
        public int points() {
            // FIFA 2025 point weights applied to the stages simulated by Brasfoot.
            // Legacy aggregated records cannot establish the precise stage history.
            if(mainMatches==0)return wins*(confed==0?2:3)+draws;
            if(confed==0)return wins*2+draws+(groupStage?4:0)
                    +(knockoutRounds>0?5+knockoutRounds-1:0);
            return wins*3+draws+3*(knockoutRounds+(groupStage?1:0));
        }
    }
    public static class Qualified implements Serializable {
        private static final long serialVersionUID=1L;
        public ah club;
        public String reason;
        public int points;
        public Qualified() {}
        Qualified(ah club,String reason,int points){this.club=club;this.reason=reason;this.points=points;}
    }
    public static class WorldCup extends Cup implements StageHost, Serializable {
        private static final long serialVersionUID=1L;
        public Groups groupPhase;
        public ArrayList<Qualified> qualified=new ArrayList<>();
        public int plannedYear;
        public int startedYear;
        public String status="Aguardando a edição de 2025";
        public WorldCup(){super("Mundial de Clubes","club_world_cup",70);}
        public boolean isEdition(int year){return year>=Rules.WORLD_FIRST_YEAR&&(year-Rules.WORLD_FIRST_YEAR)%Rules.WORLD_INTERVAL==0;}
        @Override public Groups groups(){return groupPhase;}
        @Override public L[] mB(){
            ArrayList<L> phases=new ArrayList<>();if(groupPhase!=null)phases.add(groupPhase);
            phases.addAll(rounds);return phases.toArray(new L[0]);
        }
        void inactive(int year){
            reset(year);groupPhase=null;
            int next=year<Rules.WORLD_FIRST_YEAR?Rules.WORLD_FIRST_YEAR:Rules.WORLD_FIRST_YEAR+((year-Rules.WORLD_FIRST_YEAR)/Rules.WORLD_INTERVAL+1)*Rules.WORLD_INTERVAL;
            status="Próxima edição: "+next;
        }
        public void start(Intercontinental root){
            if(!isEdition(year())||startedYear==year())return;
            root.captureSeason();
            ArrayList<Qualified> selection=select(root,year());
            reset(year());qualified=selection;
            for(Qualified entry:selection)entrants.add(entry.club);
            ArrayList<ah> draw=draw(entrants,year());
            groupPhase=new Groups(this,draw);startedYear=year();
            Rewards.entry(this,year(),entrants);
            status="Em disputa — "+year()+"; classificação pelo histórico continental da carreira";
        }
        public ArrayList<Qualified> select(Intercontinental root,int edition){
            int first=edition-4,last=edition-1;
            LinkedHashMap<Integer,Qualified> selected=new LinkedHashMap<>();
            at[] competitions=continents();
            for(int conf=0;conf<6;conf++){
                LinkedHashMap<Integer,ah> champions=new LinkedHashMap<>();
                HashMap<Integer,Integer> latest=new HashMap<>();
                if(edition==Rules.WORLD_FIRST_YEAR){
                    for(int n=0;n<Rules.SEED_REFS.length;n++)if(Rules.SEED_CONFEDS[n]==conf){
                        ah seed=null;
                        for(ah club:clubs())if(normalize(ref(club)).equals(normalize(Rules.SEED_REFS[n]))){seed=club;break;}
                        if(seed==null)throw new IllegalStateException("Clube histórico ausente: "+Rules.SEED_REFS[n]);
                        champions.put(seed.lk(),seed);latest.put(seed.lk(),2021);
                    }
                }
                for(int y=first;y<=last;y++){
                    int season=y-c.a.SR.op()+1;
                    ah champion=competitions[conf]==null||season<1?null:competitions[conf].cv(season);
                    if(champion!=null){champions.put(champion.lk(),champion);latest.put(champion.lk(),y);}
                }
                ArrayList<ah> ranked=new ArrayList<>();
                for(ah club:clubs())if(confederation(club)==conf&&points(root,club,conf,first,last)>0)ranked.add(club);
                final int cf=conf;
                Comparator<ah> ranking=Comparator.<ah>comparingInt(club->points(root,club,cf,first,last)).reversed()
                        .thenComparing(Comparator.<ah>comparingInt(club->latest.getOrDefault(club.lk(),0)).reversed())
                        .thenComparing(club->normalize(ref(club)));
                ranked.sort(ranking);
                ArrayList<ah> championsRanked=new ArrayList<>(champions.values());championsRanked.sort(ranking);
                HashMap<Integer,Integer> countries=new HashMap<>();int used=0;
                for(ah club:championsRanked){
                    if(used==Rules.SLOTS[conf])break;
                    selected.put(club.lk(),new Qualified(club,"Campeão "+Rules.CONFEDERATIONS[conf]+" "+latest.get(club.lk()),points(root,club,conf,first,last)));
                    countries.merge(club.getPais(),1,Integer::sum);used++;
                }
                if(conf!=5)for(ah club:ranked){
                    if(used==Rules.SLOTS[conf])break;
                    if(selected.containsKey(club.lk())||countries.getOrDefault(club.getPais(),0)>=2)continue;
                    selected.put(club.lk(),new Qualified(club,"Ranking da carreira — "+Rules.CONFEDERATIONS[conf],points(root,club,conf,first,last)));
                    countries.merge(club.getPais(),1,Integer::sum);used++;
                }
                if(used!=Rules.SLOTS[conf])throw new IllegalStateException("Histórico insuficiente para "+Rules.CONFEDERATIONS[conf]+": "+used+"/"+Rules.SLOTS[conf]+" vagas");
            }
            // USA is the configured host; reserve a distinct club, not an extra continental place.
            ah host=null;best.Z country=c.a.SR.o(Rules.HOST_COUNTRY_ID);
            if(country!=null&&country.eb().size()>0){
                at national=(at)country.eb().get(0);
                ah champion=national.cv(last-c.a.SR.op()+1);
                if(champion!=null&&!selected.containsKey(champion.lk()))host=champion;
            }
            String hostReason="Campeão nacional do país-sede (EUA)";
            if(host==null){
                ArrayList<ah> hostRank=new ArrayList<>();
                for(ah club:clubs())if(club.getPais()==Rules.HOST_COUNTRY_ID&&!selected.containsKey(club.lk())&&points(root,club,4,first,last)>0)hostRank.add(club);
                hostRank.sort(Comparator.<ah>comparingInt(club->points(root,club,4,first,last)).reversed().thenComparing(club->normalize(ref(club))));
                if(!hostRank.isEmpty())host=hostRank.get(0);
                hostReason="Vaga da sede (EUA): ranking continental disponível";
            }
            if(host==null)throw new IllegalStateException("País-sede sem campeão nacional ou clube distinto com histórico continental");
            selected.put(host.lk(),new Qualified(host,hostReason,points(root,host,4,first,last)));
            if(selected.size()!=32)throw new IllegalStateException("Classificação não produziu 32 clubes únicos");
            return new ArrayList<>(selected.values());
        }
        static int points(Intercontinental root,ah club,int conf,int first,int last){
            int points=0;for(SeasonScore score:root.history)if(score.clubId==club.lk()&&score.confed==conf&&score.year>=first&&score.year<=last)points+=score.points();
            return points;
        }
        static ArrayList<ah> draw(List<ah> teams,int year){
            // Backtrack over shuffled pots to enforce confederation/country restrictions.
            ArrayList<ah> order=new ArrayList<>(teams);Collections.shuffle(order,new Random(year));
            order.sort(Comparator.comparingInt((ah t)->confederation(t)==0?1:0));
            ArrayList<ArrayList<ah>> groups=new ArrayList<>();for(int n=0;n<8;n++)groups.add(new ArrayList<>());
            if(!assign(order,0,groups,new HashSet<>()))throw new IllegalStateException("Não foi possível sortear grupos com restrições por país/confederação");
            ArrayList<ah> result=new ArrayList<>();for(List<ah> group:groups)result.addAll(group);return result;
        }
        static boolean assign(List<ah> clubs,int offset,ArrayList<ArrayList<ah>> groups,Set<String> failed){
            if(offset==clubs.size())return true;
            ArrayList<String> signatures=new ArrayList<>();
            for(List<ah> group:groups){ArrayList<Integer> countries=new ArrayList<>();for(ah member:group)countries.add(member.getPais());Collections.sort(countries);signatures.add(countries.toString());}
            Collections.sort(signatures);String memo=offset+":"+signatures;
            if(failed.contains(memo))return false;
            int[] remaining=new int[6];for(int n=offset;n<clubs.size();n++)remaining[confederation(clubs.get(n))]++;
            int emptyEurope=0;int[] capacity=new int[6];
            for(List<ah> group:groups){
                int[] counts=new int[6];for(ah member:group)counts[confederation(member)]++;
                if(counts[0]==0)emptyEurope++;
                for(int c=0;c<6;c++)capacity[c]+=Math.min(4-group.size(),Math.max(0,(c==0?2:1)-counts[c]));
            }
            if(emptyEurope>remaining[0])return false;
            for(int c=0;c<6;c++)if(remaining[c]>capacity[c])return false;
            ah club=clubs.get(offset);int conf=confederation(club);
            Set<String> equivalent=new HashSet<>();
            ArrayList<ArrayList<ah>> candidates=new ArrayList<>(groups);candidates.sort(Comparator.comparingInt(List::size));
            for(ArrayList<ah> group:candidates){
                if(group.size()==4)continue;
                String shape="";boolean allowed=true;int same=0;
                for(ah member:group){shape+=member.getPais()+",";if(member.getPais()==club.getPais())allowed=false;if(confederation(member)==conf)same++;}
                if(!equivalent.add(shape))continue;
                if(!allowed||same>=(conf==0?2:1))continue;
                if(conf!=0&&group.size()==3&&group.stream().noneMatch(t->confederation(t)==0))continue;
                group.add(club);if(assign(clubs,offset+1,groups,failed))return true;group.remove(group.size()-1);
            }
            failed.add(memo);
            return false;
        }
        void groupsCompleted(){
            ArrayList<ah> pairs=new ArrayList<>();int[] order={0,3,4,7,8,11,12,15,2,1,6,5,10,9,14,13};
            ArrayList<ah> qualified=new ArrayList<>();
            for(Object object:groupPhase.yQ()){
                List<ah> group=((best.G)object).gR();qualified.add(group.get(0));qualified.add(group.get(1));
            }
            for(int index:order)pairs.add(qualified.get(index));
            rounds.add(new Stage(this,this,"Oitavas de final",16,pairs));
        }
        @Override public void completed(Stage stage,ArrayList<ah> winners){
            if(winners.size()>1){int size=winners.size();String label=size==8?"Quartas de final":size==4?"Semifinais":"Final";
                rounds.add(new Stage(this,this,label,size,winners));
            }else{
                I match=(I)((f.G)stage.zp().get(0)).zW().get(0);ah champion=winners.get(0);
                award(stage,champion,champion==match.hc()?match.hd():match.hc());status="Campeão: "+champion.getNome();
            }
        }
    }

    /** Original fixture generation and player statistics, with head-to-head standings. */
    public static class Groups extends f.s implements Serializable {
        private static final long serialVersionUID=1L;
        public WorldCup owner;
        public int playedRounds;
        public ArrayList<I> played=new ArrayList<>();
        public HashMap<Integer,Integer> lots=new HashMap<>();
        public Groups(){}
        Groups(WorldCup owner,ArrayList<ah> draw){
            super(options(),draw,0,null,null,null,5,null,false,null,true,owner);
            this.owner=owner;setNome(owner.getNome());
            ArrayList<ah> lottery=new ArrayList<>(draw);Collections.shuffle(lottery,new Random(year()^0x5EED));
            for(int n=0;n<lottery.size();n++)lots.put(lottery.get(n).lk(),n);
        }
        static LoadLigaOptions options(){
            LoadLigaOptions options=new LoadLigaOptions();options.nTimes=32;options.nGrupos=8;
            options.doisTurnos=false;options.numeroTimesMataMata=0;options.jogosDentroGrupo=true;return options;
        }
        @Override public String io(){return "Fase de grupos — rodada "+Math.min(3,playedRounds+1);}
        @Override public int zb(){return playedRounds+1;}
        @Override public void pO(){
            if(playedRounds>=3)return;
            for(Object object:day(c.a.SR.J()).h()){
                I match=(I)object;if(match.ht()!=this||played.contains(match))continue;
                match.hc().a(match,this);match.hd().a(match,this);played.add(match);
            }
            playedRounds++;
            for(Object object:yQ())sortGroup(((best.G)object).gR());
            if(playedRounds==3)owner.groupsCompleted();
        }
        int[] table(ah club,Collection<ah> restricted){
            int[] result=new int[5];
            for(I match:played){
                if(restricted!=null&&(!restricted.contains(match.hc())||!restricted.contains(match.hd())))continue;
                int own,other;if(match.hc()==club){own=match.hu();other=match.hw();}else if(match.hd()==club){own=match.hw();other=match.hu();}else continue;
                result[0]+=own>other?3:own==other?1:0;result[1]+=own-other;result[2]+=own;result[3]++;if(own>other)result[4]++;
            }
            return result;
        }
        void sortGroup(List<ah> clubs){
            clubs.sort(Comparator.<ah>comparingInt(club->table(club,null)[0]).reversed());
            int start=0;
            while(start<clubs.size()){
                int end=start+1;int points=table(clubs.get(start),null)[0];
                while(end<clubs.size()&&table(clubs.get(end),null)[0]==points)end++;
                if(end-start>1){
                    ArrayList<ah> tied=new ArrayList<>(clubs.subList(start,end));
                    List<ah> ranked=resolveTie(tied);for(int n=0;n<ranked.size();n++)clubs.set(start+n,ranked.get(n));
                }
                start=end;
            }
        }
        int compareHead(ah a,ah b,Collection<ah> tied){
            int[] x=table(a,tied),y=table(b,tied);
            for(int n=0;n<3;n++){int difference=Integer.compare(y[n],x[n]);if(difference!=0)return difference;}
            return 0;
        }
        List<ah> resolveTie(List<ah> tied){
            ArrayList<ah> result=new ArrayList<>(tied);result.sort((a,b)->compareHead(a,b,tied));
            if(compareHead(result.get(0),result.get(result.size()-1),tied)==0){
                result.sort((a,b)->{
                    int[] x=table(a,null),y=table(b,null);
                    for(int n=1;n<3;n++){int difference=Integer.compare(y[n],x[n]);if(difference!=0)return difference;}
                    int discipline=Integer.compare(fairPlay(b),fairPlay(a));if(discipline!=0)return discipline;
                    return Integer.compare(lots.getOrDefault(a.lk(),a.lk()),lots.getOrDefault(b.lk(),b.lk()));
                });
                return result;
            }
            int start=0;
            while(start<result.size()){
                int end=start+1;while(end<result.size()&&compareHead(result.get(start),result.get(end),tied)==0)end++;
                if(end-start>1){List<ah> reduced=resolveTie(new ArrayList<>(result.subList(start,end)));for(int n=0;n<reduced.size();n++)result.set(start+n,reduced.get(n));}
                start=end;
            }
            return result;
        }
        int fairPlay(ah club){
            int penalty=0;
            for(I match:played){
                if(match.hc()!=club&&match.hd()!=club)continue;
                IdentityHashMap<best.F,int[]> players=new IdentityHashMap<>();
                for(Object object:match.hE()){
                    best.A event=(best.A)object;
                    if(event.cu()!=club||event.eo()==null||event.b()<2||event.b()>4)continue;
                    int[] cards=players.computeIfAbsent(event.eo(),p->new int[3]);
                    cards[event.b()-2]=1;
                }
                for(int[] cards:players.values())penalty+=cards[1]>0?3:cards[2]>0?(cards[0]>0?5:4):cards[0]>0?1:0;
            }
            return -penalty;
        }
    }

    /** Called before the original competition-panel dispatcher. */
    public static boolean panel(JPanel panel,at competition){
        if(panel instanceof a.al)return nativePanel(panel,competition);
        Object previous=panel.getClientProperty("mod.layout");
        if(previous!=null){
            panel.removeAll();panel.setLayout((LayoutManager)previous);
            for(Component component:(Component[])panel.getClientProperty("mod.children"))panel.add(component);
            panel.putClientProperty("mod.layout",null);
        }
        if(!(competition instanceof ViewCup)||competition instanceof Intercontinental&&year()<Rules.INTERCONTINENTAL_FIRST_YEAR)return false;
        panel.putClientProperty("mod.layout",panel.getLayout());panel.putClientProperty("mod.children",panel.getComponents());
        panel.removeAll();panel.setLayout(new BorderLayout());
        ViewCup cup=(ViewCup)competition;
        JPanel heading=new JPanel(new BorderLayout());JLabel title=new JLabel(competition.getNome());title.setFont(title.getFont().deriveFont(Font.BOLD,20));
        heading.add(title,BorderLayout.CENTER);JLabel image=new JLabel(icon(cup.trophy(),120));heading.add(image,BorderLayout.WEST);panel.add(heading,BorderLayout.NORTH);
        JTabbedPane tabs=new JTabbedPane();
        ArrayList<Object[]> rows=new ArrayList<>();
        Groups group=cup.groups();
        if(group!=null){
            ArrayList<Object[]> standings=new ArrayList<>();int n=0;
            for(Object object:group.yQ()){
                for(Object member:((best.G)object).gR()){
                    ah club=(ah)member;int[] score=group.table(club,null);
                    standings.add(new Object[]{""+(char)('A'+n),club.getNome(),score[0],score[3],score[4],score[1],score[2]});
                }n++;
            }
            tabs.add("Grupos",table(new String[]{"Grupo","Clube","P","J","V","SG","GP"},standings));
            for(I match:group.played)rows.add(matchRow("Grupos",match,true));
            for(Object date:group.yF())for(Object match:day((Integer)date).h())if(((I)match).ht()==group&&!group.played.contains(match))rows.add(matchRow("Grupos",(I)match,false));
        }
        for(Stage stage:cup.stages())for(Object round:stage.zp())for(Object object:((f.G)round).zW())rows.add(matchRow(stage.label,(I)object,stage.finished));
        tabs.add("Partidas",table(new String[]{"Fase","Data","Clube","Placar","Adversário"},rows));
        if(competition instanceof WorldCup){
            WorldCup world=(WorldCup)competition;ArrayList<Object[]> qualification=new ArrayList<>();
            for(Qualified entry:world.qualified)qualification.add(new Object[]{entry.club.getNome(),Rules.CONFEDERATIONS[entry.club.gg()],entry.points,entry.reason});
            tabs.add("Classificação ao Mundial",table(new String[]{"Clube","Confederação","Pontos","Vaga"},qualification));
            heading.add(new JLabel(world.status),BorderLayout.SOUTH);
        }
        ArrayList<Object[]> titles=new ArrayList<>();
        List<at> cups=new ArrayList<>();cups.add(competition);
        if(competition instanceof Intercontinental){Intercontinental root=(Intercontinental)competition;cups.add(root.afro);cups.add(root.americas);cups.add(root.challenger);}
        for(at titleCup:cups)for(Object object:titleCup.mn()){
            i titleRecord=(i)object;ah winner=titleRecord.ce();
            titles.add(new Object[]{c.a.SR.op()+titleRecord.H()-1,titleCup.getNome(),winner==null?"—":winner.getNome()});
        }
        tabs.add("Títulos",table(new String[]{"Ano","Taça","Campeão"},titles));
        rewardsTab(tabs,competition);
        panel.add(tabs,BorderLayout.CENTER);panel.revalidate();panel.repaint();return true;
    }
    static Object member(Object object,String name){
        try{Field field=object.getClass().getDeclaredField(name);field.setAccessible(true);return field.get(object);}
        catch(ReflectiveOperationException e){throw new IllegalStateException("Tela incompatível: "+name,e);}
    }
    static void rewardsTab(JTabbedPane tabs,at cup){
        String id=Rewards.id(cup);if(id==null)return;
        ArrayList<Object[]> rows=new ArrayList<>();
        int entry=Rules.participation(id);
        if(entry>0)rows.add(new Object[]{"Participação",best.C.c(entry),"—","—"});
        if(cup instanceof WorldCup){
            String[] names={"Vitória nas oitavas","Vitória nas quartas","Vitória na semifinal","Título do Mundial"};
            for(int n=0;n<names.length;n++)rows.add(new Object[]{names[n],best.C.c(Rules.WORLD_WIN[n]),"—","—"});
        }else{
            String[] names={"Vitória no play-off","Título África–Ásia–Pacífico","Título Derby das Américas","Título Challenger Cup","Título Intercontinental"};
            int first=cup instanceof Intercontinental?0:id.equals("africa_asia_pacific")?0:id.equals("derby_of_americas")?2:3;
            int last=cup instanceof Intercontinental?4:id.equals("africa_asia_pacific")?1:first;
            if(!(cup instanceof Intercontinental))rows.add(new Object[]{"Participação paga pela Intercontinental",best.C.c(Rules.participation("intercontinental_cup")),"—","—"});
            for(int n=first;n<=last;n++)rows.add(new Object[]{names[n],best.C.c(Rules.INTER_WIN[n]),"—","—"});
        }
        int total=cup instanceof WorldCup?32:cup instanceof Intercontinental?6:id.equals("africa_asia_pacific")?3:2;
        for(int rank=1;rank<=total;){
            int last=rank;
            while(last<total&&Rules.placement(id,last+1,false)==Rules.placement(id,rank,false)&&Rules.placement(id,last+1,true)==Rules.placement(id,rank,true))last++;
            String label=rank==last?rank+"º lugar":rank+"º a "+last+"º lugares";
            rows.add(new Object[]{label,"—",Rules.placement(id,rank,false),Rules.placement(id,rank,true)});rank=last+1;
        }
        JPanel view=new JPanel(new BorderLayout());
        view.add(new JLabel("Prêmios acumulativos. Partidas: 3 pontos por vitória e 1 por empate no ranking de clubes."),BorderLayout.NORTH);
        view.add(table(new String[]{"Etapa / colocação","Premiação","Ranking do clube","Ranking do técnico"},rows),BorderLayout.CENTER);
        tabs.add("Premiações e ranking",view);
    }
    static boolean nativePanel(JPanel nativePanel,at competition){
        JScrollPane viewport=(JScrollPane)member(nativePanel,"ut");
        Object original=viewport.getClientProperty("mod.originalView");
        if(original!=null){
            viewport.setViewportView((Component)original);viewport.putClientProperty("mod.originalView",null);
            for(String field:new String[]{"wP","wQ","wi"})((Component)member(nativePanel,field)).setVisible(true);
        }
        if(!(competition instanceof ViewCup)||competition instanceof Intercontinental&&year()<Rules.INTERCONTINENTAL_FIRST_YEAR)return false;
        JPanel content=new JPanel();panel(content,competition);
        viewport.putClientProperty("mod.originalView",viewport.getViewport().getView());viewport.setViewportView(content);
        for(String field:new String[]{"wP","wQ","wi","wS"})((Component)member(nativePanel,field)).setVisible(false);
        nativePanel.revalidate();nativePanel.repaint();return true;
    }
    static Object[] matchRow(String phase,I match,boolean complete){
        String date="—";
        for(int n=0;n<c.a.SR.R().size();n++)if(day(n).h().contains(match)){date=day(n).f();break;}
        return new Object[]{phase,date,match.hc().getNome(),complete?match.hu()+" × "+match.hw():"—",match.hd().getNome()};
    }
    static JScrollPane table(String[] columns,List<Object[]> rows){
        DefaultTableModel model=new DefaultTableModel(rows.toArray(new Object[0][]),columns){@Override public boolean isCellEditable(int r,int c){return false;}};
        JTable table=new JTable(model);table.setRowHeight(24);table.setAutoCreateRowSorter(true);
        for(int n=0;n<columns.length;n++){
            javax.swing.table.TableColumn column=table.getColumnModel().getColumn(n);String name=columns[n];
            if(Arrays.asList("Grupo","P","J","V","SG","GP","Ano","Placar","Pontos").contains(name)){
                column.setMaxWidth(name.equals("Grupo")?60:80);column.setPreferredWidth(60);
            }else column.setPreferredWidth(name.equals("Vaga")?420:220);
        }
        JScrollPane scroll=new JScrollPane(table);scroll.setColumnHeaderView(table.getTableHeader());return scroll;
    }
    public static void trophyRoom(JPanel room,ah club){NativeUi.gallery(room,club);}
    public static int[] trophySize(int[] original,JPanel room){
        Object replacement=room.getClientProperty("mod.trophySize");return replacement instanceof int[]?(int[])replacement:original;
    }
    public static int trophyCount(int original,JPanel room){
        Object count=room.getClientProperty("mod.trophyGroups");
        // The original dialog chooses its height from this count; allow a full card row.
        return count instanceof Integer?(Integer)count:original;
    }
}
