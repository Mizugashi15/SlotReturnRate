package net.minezero.slotreturnrate;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

public final class SlotReturnRate extends JavaPlugin {

    JavaPlugin plugin;
    String prefix = "[§eMineZero§2Slot§r]";
    List<String> filenames = new ArrayList<>();
    List<String> symbolAmount = new ArrayList<>();

    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;
        File file = new File("plugins/MineZeroSlot/slots");

        for (File f : file.listFiles()) {

            if (f.getName().substring(f.getName().lastIndexOf(".") + 1).equalsIgnoreCase("yml")) {
                filenames.add(f.getName().substring(0, f.getName().indexOf(".")));
            }
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (command.getName().equalsIgnoreCase("slotreturn")) {

            if (args.length == 2) {

                if (!isNumber(args[1])) {
                    sender.sendMessage(prefix + " §c数字で入力してください！");
                    return false;
                }

                int n = Integer.parseInt(args[1]);
                n -= 1;

                File file = new File("plugins/MineZeroSlot/slots/" + args[0] + ".yml");
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);

                int coin = config.getInt("coin");
                double raisepot = config.getDouble("stock.raise");
                List<String> win = new ArrayList<>();
                HashMap<String,Integer> win_chance = new HashMap<>();
                HashMap<String, List<String>> actions = new HashMap<>();
                HashMap<String, Boolean> pot_out = new HashMap<>();
                int allchance = config.getInt("chance");
                for (String key : config.getConfigurationSection("win").getKeys(false)) {
                    win.add(key);
                    win_chance.put(key, config.getInt("win." + key + ".chance"));
                    allchance += config.getInt("win." + key + ".chance");
                    try {
                        actions.put(key, config.getStringList("win." + key + ".actions"));
                    } catch (NullPointerException ignore) {
                    }
                    try {
                        pot_out.put(key, config.getBoolean("win." + key + ".pot"));
                    } catch (NumberFormatException ignore) {
                    }
                }
                double pot = config.getDouble("stock.default");
                Random random = new Random();

                int totalcoin = 0;
                int totaltry = 0;
                int totalhit = 0;
                double totalmoney = 0;
                float totalchance = 0;

                for (int i = 0 ; i <= n ; i++) {

                    int chance = config.getInt("chance");
                    totalcoin += coin;
                    totaltry += 1;
                    pot += raisepot;
                    double randomnum = random.nextDouble(allchance);

                    if (randomnum > chance) {

                        for (String s : win) {

                            if (randomnum > chance) {

                                if (!actions.isEmpty()) {

                                    for (String s1 : actions.get(s)) {

                                        double amount = Double.parseDouble(s1.substring(s1.lastIndexOf(":") + 1));

                                        if (s1.contains("MULTI")) {

                                            pot = pot * amount;
                                        }
                                        if (s1.contains("RAISE")) {

                                            pot = pot + amount;
                                        }
                                    }
                                }
                                if (pot_out.get(s)) {
                                    totalmoney += pot;
                                    pot = config.getDouble("stock.default");
                                }
                                break;
                            }
                            chance += win_chance.get(s);
                        }
                        totalhit += 1;
                    }

                }

                totalcoin = totalcoin * 100;
                totalchance = (float) (totalmoney / totalcoin) * 100;

                String jpycoin = getJpyBal(totalcoin);
                String jpymoney = getJpyBal(totalmoney);

                sender.sendMessage(prefix);
                sender.sendMessage(" §eスロット名 " + args[0]);
                sender.sendMessage(" §7" + totaltry + " 回転 §fして");
                sender.sendMessage(" §e§l" + totalhit + " 回 §r当たりが出ました");
                sender.sendMessage(" §e" + jpycoin + "円 §fが投資で");
                sender.sendMessage(" §e§l" + jpymoney + "円 §rが排出額です");
                sender.sendMessage(" 還元率は§c" + totalchance + " % §rです");

                return true;
            }

            return true;
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return strings(args[0], filenames);
        }
        return null;
    }

    private List<String> strings(String s,List<String> args){
        List<String> list = new ArrayList<>();
        for(String s1:args){
            if(s1.startsWith(s))
                list.add(s1);
        }
        return list;
    }

    public boolean isNumber(String s) {
        try {
            int i = Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public String getJpyBal(double amount) {
        long money = (long) amount;

        if (money < 10000){
            return String.valueOf(money);
        }

        if (money < 100000000){

            long man = (long) (money/10000);
            String left = String.valueOf(money).substring(String.valueOf(money).length()-4);
//            String.valueOf(money).substring(0, String.valueOf(money).indexOf(".")).length()
            if (Long.parseLong(left) == 0){
                return man + "万";
            }

            return man + "万" + Long.parseLong(left);
        }

        if (money < 100000000000L){

            long oku = (long) (money/100000000);
            String man = String.valueOf(money).substring(String.valueOf(money).length() -8);
            String te = man.substring(0, 4);
            String left = String.valueOf(money).substring(String.valueOf(money).length() -4);

            if (Long.parseLong(te)  == 0){

                if (Long.parseLong(left) == 0){
                    return oku + "億";
                } else {
                    return oku + "億"+ Long.parseLong(left);
                }

            } else {

                if (Long.parseLong(left) == 0){
                    return oku + "億" + Long.parseLong(te) + "万";
                }
            }

            return oku + "億" + Long.parseLong(te) + "万" + Long.parseLong(left);
        }

        return "null";
    }
}
