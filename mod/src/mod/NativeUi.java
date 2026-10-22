// SPDX-License-Identifier: GPL-3.0-only
package mod;

import best.ah;
import best.aj;
import best.ao;
import best.at;
import best.i;
import java.awt.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import static mod.CompetitionMod.*;

/** Extend the original widgets and models without replacing the game's theme. */
@SuppressWarnings({"unchecked","rawtypes"})
final class NativeUi {
    private NativeUi(){}
    static void set(Object object,String name,Object value){
        try{Field field=object.getClass().getDeclaredField(name);field.setAccessible(true);field.set(object,value);}
        catch(ReflectiveOperationException error){throw new IllegalStateException(name,error);}
    }
    static LinkedHashMap<String,ArrayList<ao>> categories(ah club){
        LinkedHashMap<String,ArrayList<ao>> groups=new LinkedHashMap<>();
        for(Object object:club.cT()){
            ao title=(ao)object;if(title.ct()<0||title.gS()==null)continue;
            groups.computeIfAbsent(title.b()+":"+title.el(),key->new ArrayList<>()).add(title);
        }
        return groups;
    }
    static String years(List<ao> titles){
        ArrayList<Integer> years=new ArrayList<>();for(ao title:titles)years.add(c.a.SR.op()+title.H()-1);
        years.sort(Collections.reverseOrder());StringBuilder result=new StringBuilder("<html>");
        for(int n=0;n<years.size();n++){if(n>0)result.append(", ");result.append(years.get(n));}
        return result.append("</html>").toString();
    }
    static ImageIcon image(at cup){
        String id=Rewards.id(cup);
        if(id!=null&&(!(cup instanceof Intercontinental)||year()>=Rules.INTERCONTINENTAL_FIRST_YEAR)){
            ImageIcon image=icon(id,128);if(image!=null){
                java.awt.image.BufferedImage source=(java.awt.image.BufferedImage)image.getImage();
                int left=128,top=128,right=0,bottom=0;
                for(int y=0;y<128;y++)for(int x=0;x<128;x++)if((source.getRGB(x,y)>>>24)>16){left=Math.min(left,x);right=Math.max(right,x);top=Math.min(top,y);bottom=Math.max(bottom,y);}
                int width=right-left+1,height=bottom-top+1;double scale=Math.min(84.0/width,124.0/height);
                int w=Math.max(1,(int)Math.round(width*scale)),h=Math.max(1,(int)Math.round(height*scale));
                java.awt.image.BufferedImage fitted=new java.awt.image.BufferedImage(84,124,java.awt.image.BufferedImage.TYPE_INT_ARGB);
                Graphics2D graphics=fitted.createGraphics();graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                graphics.drawImage(source,(84-w)/2,(124-h)/2,(84+w)/2,(124+h)/2,left,top,right+1,bottom+1,null);graphics.dispose();
                return new ImageIcon(fitted);
            }
        }
        return best.aq.x(cup.mA()[0]);
    }
    static void gallery(JPanel room,ah club){
        if(club==null)return;loaded();
        LinkedHashMap<String,ArrayList<ao>> groups=categories(club);
        if(groups.values().stream().noneMatch(t->t.get(0).gS() instanceof ViewCup))return;
        if(room instanceof a.di){
            components.k[] cards=(components.k[])member(room,"ER");
            try{
                Method hide=components.k.class.getDeclaredMethod("at",boolean.class);hide.setAccessible(true);
                for(components.k card:cards)hide.invoke(card,false);
            }catch(ReflectiveOperationException error){throw new IllegalStateException(error);}
            int count=0;ArrayList<components.k> populated=new ArrayList<>();
            for(ArrayList<ao> titles:groups.values()){
                components.k card=count<cards.length?cards[count]:new components.k();at cup=titles.get(0).gS();
                card.a(cup.getNome(),years(titles),titles.size(),image(cup));
                JLabel name=(JLabel)member(card,"Ov");name.setToolTipText(cup.getNome());
                if(cup.getNome().length()>27)name.setFont(name.getFont().deriveFont(10f));
                populated.add(card);count++;
            }
            set(room,"EQ",count);
            if(count>cards.length){
                for(components.k card:cards)room.remove(card);
                JPanel grid=new JPanel(new GridLayout(0,4,15,16));grid.setBackground(room.getBackground());
                for(components.k card:populated){card.setPreferredSize(new Dimension(200,164));grid.add(card);}
                JScrollPane scroll=new JScrollPane(grid);scroll.setBorder(null);
                room.add(scroll,new components.a(20,40,845,705));
            }
        }else if(room instanceof a.de){
            ArrayList<components.aj> rows=new ArrayList<>();int column=0;components.aj row=null;
            for(ArrayList<ao> titles:groups.values()){
                if(column==0){row=new components.aj();rows.add(row);}
                at cup=titles.get(0).gS();String filename=cup.mA()[0];
                if(!(cup instanceof ViewCup))try{
                    Method original=a.de.class.getDeclaredMethod("a",ao.class);original.setAccessible(true);filename=(String)original.invoke(room,titles.get(0));
                }catch(ReflectiveOperationException error){throw new IllegalStateException(error);}
                row.d(cup.getNome(),column);row.e(System.getProperty("user.dir")+"/trofeus/"+filename+".png",column);row.R(titles.size(),column);
                column=(column+1)%5;
            }
            set(room,"En",rows);set(room,"Ep",column==0?5:column);set(room,"Eo",5);
            ((JTable)member(room,"Eq")).setModel(new b.y(rows));
            room.putClientProperty("mod.trophySize",new int[]{rows.size(),Math.min(5,groups.size())});
        }
        room.putClientProperty("mod.trophyGroups",groups.size());room.revalidate();room.repaint();
    }
    static class RankRow extends components.ad {
        final String name;
        RankRow(ah club,String name,int[] values,int mode){
            this.name=name;n(club);a(5);m(values);dR(mode);
        }
    }
    static Component rankCell(Component rendered,Object value,int column){
        if(column==0&&value instanceof RankRow&&rendered instanceof JLabel)((JLabel)rendered).setText(((RankRow)value).name);
        return rendered;
    }
    static int placement(at cup,ah club,int civilYear){
        int season=civilYear-c.a.SR.op()+1;
        for(Object object:club.le()){
            aj stats=(aj)object;
            if(stats.H()==season&&stats.b()==5&&stats.el()==cup.el()&&stats.lu()>0)return stats.lu();
        }
        for(Object object:cup.mn()){
            i record=(i)object;if(record.H()!=season)continue;
            if(record.ce()==club)return 1;if(record.cf()==club)return 2;
        }
        return 0;
    }
    static int placementPoints(at cup,ah club){
        int total=0;
        for(SeasonScore receipt:state().history){
            if(receipt.clubId!=club.lk()||receipt.origin==null||!receipt.origin.startsWith("reward:v1:"))continue;
            if(!receipt.origin.equals(Rewards.key(cup,receipt.year,"placement",club)))continue;
            total+=Rules.placement(Rewards.id(cup),placement(cup,club,receipt.year),false);
        }
        return total;
    }
    static ArrayList<RankRow> breakdown(ah club,int mode){
        Intercontinental root=state();ArrayList<RankRow> result=new ArrayList<>();if(root==null)return result;
        List<at> cups=Arrays.asList(root.world,root,root.afro,root.americas,root.challenger);
        int[] remainder=mode==2?new int[]{club.la()[5],0,0}:club.lc()[5].clone();
        for(at cup:cups){
            int[] values=new int[3];
            if(mode==2)values[0]=placementPoints(cup,club);
            else for(Object object:club.le()){
                aj stats=(aj)object;if(stats.b()!=5||stats.el()!=cup.el())continue;
                values[0]+=3*stats.cm()+stats.w()-stats.cm()-stats.co();values[1]+=stats.ls();values[2]+=stats.lt();
            }
            for(int n=0;n<3;n++)remainder[n]-=values[n];
            if(values[0]!=0||values[1]!=0||values[2]!=0)result.add(new RankRow(club,cup.getNome(),values,mode));
        }
        if(remainder[0]!=0||remainder[1]!=0||remainder[2]!=0)
            result.add(new RankRow(club,"Mundial de Clubes (até 2023)",remainder,mode));
        return result;
    }
    static void ranking(JPanel panel){
        if(!(panel instanceof a.hc)||state()==null)return;
        ah club=(ah)member(panel,"uk");int mode=(Integer)member(panel,"w");if(club==null||mode<1||mode>2)return;
        ArrayList<components.ad> nativeRows=(ArrayList<components.ad>)member(panel,"Fj");
        ArrayList<RankRow> detail=breakdown(club,mode);int index=nativeRows.size();
        for(int n=nativeRows.size()-1;n>=0;n--)if(nativeRows.get(n).b()==5){index=n;nativeRows.remove(n);}
        nativeRows.addAll(index,detail);panel.putClientProperty("mod.rankingRows",detail);
        JTable table=(JTable)member(panel,"Fl");
        if(table.getModel() instanceof javax.swing.table.AbstractTableModel)((javax.swing.table.AbstractTableModel)table.getModel()).fireTableDataChanged();
        table.repaint();
    }
}
