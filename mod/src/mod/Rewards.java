// SPDX-License-Identifier: GPL-3.0-only
package mod;

import best.ah;
import best.aj;
import best.al;
import best.at;
import best.i;
import java.lang.reflect.Field;
import java.util.*;
import static mod.CompetitionMod.*;

/** Official-game cash/ranking effects, with receipts in the existing save schema. */
final class Rewards {
    private Rewards(){}
    static String id(at cup){
        if(cup instanceof Intercontinental)return "intercontinental_cup";
        return cup instanceof Cup?((Cup)cup).trophyId:null;
    }
    static String key(at cup,int edition,String event,ah club){
        return "reward:v1:"+edition+":"+cup.el()+":"+event+":"+club.lk();
    }
    static boolean received(String key){
        for(SeasonScore receipt:state().history)if(key.equals(receipt.origin))return true;
        return false;
    }
    static int nativeClub(int rank){return rank==1?10:rank==2?5:0;}
    static int nativeCoach(int rank){return rank==1?25:rank==2?5:0;}
    static Field coachPoints(){
        try{Field field=al.class.getDeclaredField("oc");field.setAccessible(true);return field;}
        catch(ReflectiveOperationException e){throw new IllegalStateException("Ranking nativo do técnico incompatível",e);}
    }
    static void credit(at cup,int edition,String event,ah club,int cash,int points,al coach,int coachBonus){
        credit(cup,edition,event,club,cash,points,coach,coachBonus,3);
    }
    static void credit(at cup,int edition,String event,ah club,int cash,int points,al coach,int coachBonus,int category){
        String key=key(cup,edition,event,club);if(received(key))return;
        if(coach!=null&&coachBonus!=0)try{
            Field field=coachPoints();field.setInt(coach,Math.addExact(field.getInt(coach),coachBonus));
        }catch(IllegalAccessException e){throw new IllegalStateException(e);}
        club.la()[5]=Math.addExact(club.la()[5],points);
        if(cash>0){
            club.v(cash,category);
            if(category==3&&club.jZ()&&club.ka()!=null&&club.ka().jZ())
                new components.as(club.ka(),26,79,cup.getNome(),best.C.c(cash));
        }
        // Negative confederation isolates receipts from the continental coefficient.
        // Reusing SeasonScore avoids adding fields to already distributed Kryo classes.
        SeasonScore receipt=new SeasonScore();receipt.year=edition;receipt.confed=-1;
        receipt.clubId=club.lk();receipt.origin=key;receipt.wins=cash;receipt.draws=points;receipt.goals=coachBonus;
        state().history.add(receipt);
    }
    static void gate(best.I match){
        if(match.hc().jZ()&&match.hN()>0)
            credit(match.hy(),year(),"gate:"+match.hM()+":"+match.hd().lk(),match.hc(),match.hN(),0,null,0,5);
    }
    static void placing(at cup,ah club,int rank){
        if(id(cup)==null){club.a(cup,rank,0);return;}
        if(received(key(cup,year(),"placement",club)))return;
        // Preserve native placement statistics, trophies and reputation effects.
        club.a(cup,rank,0);
        credit(cup,year(),"placement",club,0,Rules.placement(id(cup),rank,false)-nativeClub(rank),
                club.ka(),Rules.placement(id(cup),rank,true)-nativeCoach(rank));
    }
    static void entry(at cup,int edition,Collection<ah> entrants){
        int amount=Rules.participation(id(cup));
        for(ah club:new LinkedHashSet<>(entrants))credit(cup,edition,"entry",club,amount,0,null,0);
    }
    static void stage(Stage stage,Collection<ah> winners){
        at cup=stage.yT();int amount;
        if(stage.host instanceof WorldCup){
            int index=stage.tag==16?0:stage.tag==8?1:stage.tag==4?2:3;
            amount=Rules.WORLD_WIN[index];
        }else amount=Rules.INTER_WIN[stage.tag];
        for(ah club:winners)credit(cup,stage.seasonYear,"win:"+stage.tag,club,amount,0,null,0);
    }
    static void historical(at cup,int season,ah club,int rank,al coach){
        if(club==null)return;
        boolean nativeAwarded=false;
        for(Object object:club.le()){
            aj stats=(aj)object;
            if(stats.H()==season&&stats.b()==5&&stats.el()==cup.el()&&stats.lu()==rank){nativeAwarded=true;break;}
        }
        credit(cup,c.a.SR.op()+season-1,"placement",club,0,
                Rules.placement(id(cup),rank,false)-(nativeAwarded?nativeClub(rank):0),coach,
                Rules.placement(id(cup),rank,true)-(nativeAwarded?nativeCoach(rank):0));
    }
    static void reconcile(Intercontinental root){
        ArrayList<at> cups=new ArrayList<>(root.additional());cups.add(root);
        for(at cup:cups){
            if(id(cup)==null)continue;
            for(Object object:cup.mn()){
                i record=(i)object;
                if(cup==root&&c.a.SR.op()+record.H()-1<Rules.INTERCONTINENTAL_FIRST_YEAR)continue;
                for(ah club:clubs())for(Object objectStats:club.le()){
                    aj stats=(aj)objectStats;
                    if(stats.H()==record.H()&&stats.b()==5&&stats.el()==cup.el()&&stats.lu()>0)
                        historical(cup,record.H(),club,stats.lu(),club==record.ce()?record.ci():club==record.cf()?record.cj():null);
                }
                historical(cup,record.H(),record.ce(),1,record.ci());
                historical(cup,record.H(),record.cf(),2,record.cj());
            }
        }
        // Entry fees can be repaired for active editions; old phase cash is never replayed.
        if(root.world.startedYear==year()&&root.world.cS()==null)entry(root.world,year(),root.world.entrants);
        if(root.initializedYear==year()&&root.champions!=null&&root.cS()==null)entry(root,year(),Arrays.asList(root.champions));
    }
}
