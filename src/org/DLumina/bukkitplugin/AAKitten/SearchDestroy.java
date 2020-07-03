package org.DLumina.bukkitplugin.AAKitten;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.bukkit.Particle.DustOptions;
import org.bukkit.potion.PotionEffect;

public class SearchDestroy implements Runnable {
	//������
	//�������е�è�����෽��ṹ����ⷶΧ�ڵж�ʵ�壬Ȼ�����
	//���з�Χ����èΪ���ģ�32mΪˮƽ����뾶�����߷�Χ�����ǲ�����arctg0.�����������ޣ��ڵ�����ʵ�嶼���Ա�����
	private Set<String> absence_worlds = new HashSet<String>();
	private Set<String> enemies_list = new HashSet<String>();
	double consume_fish = 0.1;
	double attack_speed = 4;//��ʱ��ÿ������ѭ������һ��
	
	private static double attack_count = 0;//ʱ�Ӽ���
	
	public SearchDestroy(Set<String> absence_worlds, Set<String> enemies_list, double consume_fish, double attack_speed) {
		this.absence_worlds = absence_worlds;
		this.enemies_list = enemies_list;
		this.consume_fish = consume_fish;
		this.attack_speed = attack_speed;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void run() {
		//System.out.println("Mainthread running");
		List<Cat> all_cats;
		for(World w : Bukkit.getServer().getWorlds()) {
			if (absence_worlds.contains(w.getName().toLowerCase()))
				continue;
			//����ʧЧ������
			all_cats = (List<Cat>) w.getEntitiesByClass(org.bukkit.entity.Cat.class);
			for (org.bukkit.entity.Cat cat : all_cats) {
				if((cat.getCustomName()==null)||(cat.isSitting()==false))
					continue;
				//û������è��Ҫ��վ�ŵĲ�Ҫ
				//System.out.println("A cat");
				Block curBlock = cat.getLocation().getBlock().getRelative(org.bukkit.block.BlockFace.DOWN);
				if(isContainer(curBlock)==false)
					continue;
				//������װ��������Ʒ������
				if (!(
						(curBlock.getRelative(org.bukkit.block.BlockFace.EAST).getBlockData().clone() instanceof org.bukkit.block.data.type.Stairs)
						&&(curBlock.getRelative(org.bukkit.block.BlockFace.WEST).getBlockData().clone() instanceof org.bukkit.block.data.type.Stairs)
						&&(curBlock.getRelative(org.bukkit.block.BlockFace.NORTH).getBlockData().clone() instanceof org.bukkit.block.data.type.Stairs)
						&&(curBlock.getRelative(org.bukkit.block.BlockFace.SOUTH).getBlockData().clone() instanceof org.bukkit.block.data.type.Stairs)
						&&(curBlock.getRelative(org.bukkit.block.BlockFace.NORTH_EAST).getBlockData().clone() instanceof org.bukkit.block.data.type.Stairs)
						&&(curBlock.getRelative(org.bukkit.block.BlockFace.NORTH_WEST).getBlockData().clone() instanceof org.bukkit.block.data.type.Stairs)
						&&(curBlock.getRelative(org.bukkit.block.BlockFace.SOUTH_EAST).getBlockData().clone() instanceof org.bukkit.block.data.type.Stairs)
						&&(curBlock.getRelative(org.bukkit.block.BlockFace.SOUTH_WEST).getBlockData().clone() instanceof org.bukkit.block.data.type.Stairs)
						))
					continue;
				//��Χ���ǰ˸�̨�׹��ɵ���
				//(curBlock.getRelative(org.bukkit.block.BlockFace.SOUTH_WEST).getBlockData().getMaterial()==org.bukkit.Material.POLISHED_ANDESITE_STAIRS)
				//System.out.println("Multiblock");
				Location celoc = cat.getEyeLocation();//è�۾���λ��
				double x0 = celoc.getX();
				double y0 = celoc.getY();
				double z0 = celoc.getZ();
				///è��xyz����
				Inventory inv = ((Container)(curBlock.getState())).getInventory();
				if(
						!((inv.contains(org.bukkit.Material.ARROW))||(inv.contains(org.bukkit.Material.GUNPOWDER))||(inv.contains(org.bukkit.Material.ICE)))
						||
						!((inv.contains(org.bukkit.Material.COD))||(inv.contains(org.bukkit.Material.TROPICAL_FISH)))
						) {
					DustOptions dustOptions = new DustOptions(org.bukkit.Color.RED, 1);
					for(double i=0;i<12;i++)
						w.spawnParticle(org.bukkit.Particle.REDSTONE, x0 + 0.5*Math.sin(i*3.1416*2/12), y0+1, z0 + 0.5*Math.cos(i*3.1416*2/12), 1, dustOptions);
					continue;
				}
				//������Ϊ�ӵ��ļ�/��ҩ/�����Լ���Ϊ������С��ɣ����û�о���ʾ��ɫ���ӣ�Ȼ���չ�
				//System.out.println("All OK");
				Chunk curChunk = curBlock.getChunk();
				int cx = curChunk.getX();
				int cz = curChunk.getZ();
				//�����xz����
				double x = 0, y = 0, z = 0;//Ŀ�굽è���������
				double Hdis = -1L;//ʵ�嵽è��ˮƽ�����ƽ��
				double dis = 10000L;//ʵ�嵽è�ľ����ƽ��
				double dist = 20000L;//��ʱ����
				Entity target = cat;//Ŀ��ʵ�壬��ʼ����è��������־
				List<Entity> all_entities = new ArrayList<Entity>();//������Ŀ���б�
				Entity[] temp = null;
				Location eloc;//Ŀ��λ�ã�Ŀ���۾�λ��
				//all_entities = curChunk.getEntities();
				temp = curChunk.getEntities();
				for (Entity e : temp) {
					all_entities.add(e);
				}
				//��ȡ�����������ʵ�壬����ʵ��϶������з�Χ��
				//���а뾶�ǹ̶���32���������25�����������ʵ��
				for (Entity e : all_entities) {
					if(!(enemies_list.contains(e.getType().getName().toLowerCase())))
						continue;
					//���������嵥������˳�
					
					if (e instanceof Player) {
						//�������ң���ô����Ҫ���������Դ���ֽ��
						boolean f = false;//��־����
						if(inv.contains(org.bukkit.Material.CREEPER_HEAD)){
							for(int i=0;i<=26;i++) {
								//����������Ʒ��
								if((inv.getItem(i)!=null)
										&&(inv.getItem(i).getType()==org.bukkit.Material.CREEPER_HEAD)
										&&(inv.getItem(i).getItemMeta().hasDisplayName()==true)
										&&(inv.getItem(i).getItemMeta().getDisplayName().equals( ((Player)e).getName() ))){
									f = true;
									break;
									//�����и����⣬�����һ���ǿյģ��ᱨ����ô�����һ���ǲ��ǿյ���
								}
							}
							if(f==false)
								continue;
						}
						else if(inv.contains(org.bukkit.Material.PAPER)) {
							for(int i=0;i<=26;i++) {
								//����������Ʒ��
								if((inv.getItem(i)!=null)
										&&(inv.getItem(i).getType()==org.bukkit.Material.PAPER)
										&&(inv.getItem(i).getItemMeta().hasDisplayName()==true)
										&&(inv.getItem(i).getItemMeta().getDisplayName().equals( ((Player)e).getName() ))){
									f = true;
									break;
								}
							}
							if(f==true)
								continue;
						}
						else
							continue;
					}
					//���¼��㵽è����ģ�ˮƽ����32m�ڵĵж�ʵ��
					//System.out.println("find enemy");
					eloc = e.getLocation();
					x = eloc.getX()-x0;
					y = eloc.getY()-y0;
					z = eloc.getZ()-z0;
					Hdis = x*x + z*z;
					if((y<0)&&(Hdis<(-y*2)))
							continue;
					//��ϵ��̨��������ͷ��ָ�������ʵ�壩
					//System.out.println("find enemy1");
					dist = Hdis+y*y;
					if((dist<dis)&&
							(w.rayTraceBlocks(celoc, new Vector(x,y,z), Math.sqrt(dist), org.bukkit.FluidCollisionMode.NEVER, true) == null)){
						//������Ǹ�
						//���������ײ����������Һ�壬�Լ�û����ײ�䵫���пɱ�ѡ������ķ��飬��߲ݺ͸�ʾ��
						dis = dist;
						target = e;
						//System.out.println("enemy locked");
					}
					else {
						Vector toEntityEye = ((LivingEntity) e).getEyeLocation().toVector().subtract(celoc.toVector());
						if(w.rayTraceBlocks(celoc, toEntityEye, toEntityEye.length(), org.bukkit.FluidCollisionMode.NEVER, true) == null) {
							dis = dist;
							target = e;
							//�������ţ������۾�Ҳ���Դ�
						}
					}
				}
				//��ǰ��������
				
				
				all_entities.clear();//��������嵥
				if(target != cat) {
					//��ǰ�����ҵ�Ŀ���ˣ�����һȦ���鲻����
					for(int i=-1;i<=1;i++) {
						for(int j=-1;j<=1;j++) {
							if(i==0&&j==0)
								continue;
							temp = w.getChunkAt(cx+i,cz+j).getEntities();
							for (Entity e : temp) {
								all_entities.add(e);
							}
							//all_entities.addAll(0, w.getChunkAt(cx+i,cz+j).getEntities());
						}
					}
				}
				else {
					for(int i=-2;i<=2;i++) {
						for(int j=-2;j<=2;j++) {
							if(i==0&&j==0)
								continue;
							temp = w.getChunkAt(cx+i,cz+j).getEntities();
							for (Entity e : temp) {
								all_entities.add(e);
							}
							//all_entities.addAll(0, w.getChunkAt(cx+i,cz+j).getEntities());
						}
					}
				}
				for (Entity e : all_entities) {
					if(!(enemies_list.contains(e.getName().toLowerCase())))
						continue;
					//ͬ�ϣ����������嵥������˳�
					eloc = e.getLocation();
					x = eloc.getX()-x0;
					y = eloc.getY()-y0;
					z = eloc.getZ()-z0;
					Hdis = x*x + z*z;
					if((y<0)&&((Hdis>1024)||(Hdis<(-y*2))))
							continue;
					//������ͷ����
					//�����һ���ǲ�����32m����ж�
					dist = Hdis+y*y;
					if(dist<dis&&
							w.rayTraceBlocks(cat.getEyeLocation(), new Vector(x,y,z), Math.sqrt(dist), org.bukkit.FluidCollisionMode.NEVER, true) == null) {
						//ͬ�ϣ������������
						dis = dist;
						target = e;
					}
					else {
						Vector toEntityEye = ((LivingEntity) e).getEyeLocation().toVector().subtract(celoc.toVector());
						if(w.rayTraceBlocks(celoc, toEntityEye, toEntityEye.length(), org.bukkit.FluidCollisionMode.NEVER, true) == null) {
							dis = dist;
							target = e;
							//ͬ�ϣ������۾�Ҳ���Դ�
						}
					}
				}
				//��Ȧ����
				
				if(target == cat) {
					DustOptions dustOptions = new DustOptions(org.bukkit.Color.GREEN, 1);
					for(double i=0;i<12;i++)
						w.spawnParticle(org.bukkit.Particle.REDSTONE, x0 + 0.5*Math.sin(i*3.1416*2/12), y0+1, z0 + 0.5*Math.cos(i*3.1416*2/12), 1, dustOptions);
					continue;
				}
				//���èû�ҵ��ж�ʵ�壬����������������ʾ��ɫ������
				
				
				DustOptions dustOptions = new DustOptions(org.bukkit.Color.BLUE, 1);
				for(double i=0;i<12;i++)
					w.spawnParticle(org.bukkit.Particle.REDSTONE, x0 + 0.5*Math.sin(i*3.1416*2/12), y0+1, z0 + 0.5*Math.cos(i*3.1416*2/12), 1, dustOptions);
				//���гɹ�����ʾ��ɫ
				attack_count += 1;
				if(attack_count < attack_speed) {
					//System.out.println("count down");
					continue;
				}//��������û����������
				attack_count = 0;
				//�������ڵ���
				Location tloc = target.getLocation();
				x = tloc.getX()-x0;
				y = tloc.getY()-y0;
				z = tloc.getZ()-z0;
				dis = Math.sqrt(x*x+y*y+z*z);//ע�����������dis�����Ǿ���ƽ���ˣ����Ǿ��뱾��
				//System.out.println("hit");
				if(inv.contains(org.bukkit.Material.ICE)) {
					//�б��������ñ�������ʩ�ӻ���������
					//System.out.println("ice");
					inv.removeItem(new ItemStack(Material.ICE));
					//����һ���
					if(Math.random()<consume_fish) {
						//èè���ˣ�èè��С���
						if(inv.contains(org.bukkit.Material.COD))
							inv.removeItem(new ItemStack(Material.COD));
						else
							inv.removeItem(new ItemStack(Material.TROPICAL_FISH));
					}
					ItemStack itemCrackData = new ItemStack(Material.ICE);
					for(double i=0;i<dis*5;i++)
						w.spawnParticle(org.bukkit.Particle.ITEM_CRACK, x0 + x*i/5, y0 + y*i/5, z0 + z*i/5, 1, itemCrackData);
					//��ʱ����ôд�ţ�˲�������Ժ��ٿ��ǵ������
					PotionEffect slow = new PotionEffect(org.bukkit.potion.PotionEffectType.SLOW, 100, 10);
					((LivingEntity)target).removePotionEffect(org.bukkit.potion.PotionEffectType.SLOW);
					((LivingEntity)target).addPotionEffect(slow);
					//System.out.println("ice hit");
					for(Entity en : target.getNearbyEntities(1.5, 1.5, 1.5)) {
						if (en instanceof LivingEntity) {
							((LivingEntity)en).removePotionEffect(org.bukkit.potion.PotionEffectType.SLOW);
							((LivingEntity)en).addPotionEffect(slow);
							//System.out.println("ice hit1");
						}
							
					}
				}
				else if(inv.contains(org.bukkit.Material.GUNPOWDER)){
					//�л�ҩ������û�ҩ���������챬ը
					//System.out.println("gunpow");
					inv.removeItem(new ItemStack(Material.GUNPOWDER));
					//����һ����ҩ
					if(Math.random()<consume_fish) {
						//èè���ˣ�èè��С���
						if(inv.contains(org.bukkit.Material.COD))
							inv.removeItem(new ItemStack(Material.COD));
						else
							inv.removeItem(new ItemStack(Material.TROPICAL_FISH));
					}
					for(double i=0;i<dis*5;i++)
						w.spawnParticle(org.bukkit.Particle.FLAME, x0 + x*i/5, y0 + y*i/5, z0 + z*i/5, 1);
					w.createExplosion(x+x0, y+y0, z+z0, 3, false, false, cat);
					//System.out.println("boom");
					//3����ը��tnt��4���������������棬���ƻ����飬��ͷ��è
				}
				else {
					//ֻ�м�������
					//System.out.println("arrow");
					inv.removeItem(new ItemStack(Material.ARROW));
					//����һ����
					if(Math.random()<consume_fish) {
						//èè���ˣ�èè��С���
						if(inv.contains(org.bukkit.Material.COD))
							inv.removeItem(new ItemStack(Material.COD));
						else
							inv.removeItem(new ItemStack(Material.TROPICAL_FISH));
					}
					for(double i=0;i<dis*5;i++)
						w.spawnParticle(org.bukkit.Particle.END_ROD, x0 + x*i/5, y0 + y*i/5, z0 + z*i/5, 1);
					((Damageable)target).damage(8,cat);
					//System.out.println("arrow hit");
					//���8���˺���������20����
				}
				//�ҵ��ж�ʵ���ˣ�����
			}
		}
	}
	private static boolean isContainer(Block block) {
		Material mat = block.getType();
        return mat == Material.BARREL || mat == Material.CHEST || mat == Material.SHULKER_BOX || mat == Material.DROPPER || mat == Material.DISPENSER || mat == Material.HOPPER;
	}
}
