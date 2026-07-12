package net.chauvedev.woodencog.compat;

import net.chauvedev.woodencog.compat.createaddition.CCAIntegrationImpl;
import net.chauvedev.woodencog.compat.createaddition.EmptyCCAIntegration;
import net.chauvedev.woodencog.compat.createaddition.ICCAIntegration;
import net.chauvedev.woodencog.compat.createdieselgenerators.CDGIntegrationImpl;
import net.chauvedev.woodencog.compat.createdieselgenerators.EmptyCDGIntegration;
import net.chauvedev.woodencog.compat.createdieselgenerators.ICDGIntegration;
import net.chauvedev.woodencog.compat.createlowheated.CLHIntegrationImpl;
import net.chauvedev.woodencog.compat.createlowheated.EmptyCLHIntegration;
import net.chauvedev.woodencog.compat.createlowheated.ICLHIntegration;
import net.chauvedev.woodencog.compat.createmoreburners.CMBIntegrationImpl;
import net.chauvedev.woodencog.compat.createmoreburners.EmptyCMBIntegration;
import net.chauvedev.woodencog.compat.createmoreburners.ICMBIntegration;
import net.chauvedev.woodencog.compat.electroenergetics.CEEIntegrationImpl;
import net.chauvedev.woodencog.compat.electroenergetics.EmptyCEEIntegration;
import net.chauvedev.woodencog.compat.electroenergetics.ICEEIntegration;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.LoadingModList;

public class Compat {

    public static final String CCA_MOD_ID = "createaddition"; // Create Crafts & Additions
    public static final String CLH_MOD_ID = "createlowheated"; // Create Low Heated
    public static final String CMB_MOD_ID = "moreburners"; // Create More Burners
    public static final String CDNDESIRE_MOD_ID = "dndesires"; // Create Dreams & Desires

    public static final String CEE_MOD_ID = "electroenergetics"; // Create Electro Energetics

    public static final String CDG_MOD_ID = "createdieselgenerators"; // Create Diesel Generators

    public static boolean isCCALoaded() {
        return ModList.get().isLoaded(CCA_MOD_ID);
    }
    public static boolean isCCAInstalled(){
        return LoadingModList.get().getModFileById(CCA_MOD_ID) != null;
    }
    public static boolean isCLHLoaded() {
        return ModList.get().isLoaded(CLH_MOD_ID);
    }
    public static boolean isCLHInstalled(){
        return LoadingModList.get().getModFileById(CLH_MOD_ID) != null;
    }
    public static boolean isCMBLoaded() {
        return ModList.get().isLoaded(CMB_MOD_ID);
    }
    public static boolean isCMBInstalled() {
        return LoadingModList.get().getModFileById(CMB_MOD_ID) != null;
    }
    public static boolean isCDNDESIRELoaded() {
        return ModList.get().isLoaded(CDNDESIRE_MOD_ID);
    }
    public static boolean isCDNDESIREInstalled() {
        return LoadingModList.get().getModFileById(CDNDESIRE_MOD_ID) != null;
    }
    public static boolean isCEELoaded() {
        return ModList.get().isLoaded(CEE_MOD_ID);
    }
    public static boolean isCEEInstalled() {
        return LoadingModList.get().getModFileById(CEE_MOD_ID) != null;
    }
    public static boolean isCDGLoaded() {
        return ModList.get().isLoaded(CDG_MOD_ID);
    }
    public static boolean isCDGInstalled() {
        return LoadingModList.get().getModFileById(CDG_MOD_ID) != null;
    }

    public static ICCAIntegration CCA_INSTANCE;
    public static ICLHIntegration CLH_INSTANCE;
    public static ICMBIntegration CMB_INSTANCE;
    public static ICEEIntegration CEE_INSTANCE;
    public static ICDGIntegration CDG_INSTANCE;

    public static void init(){
        CCA_INSTANCE = isCCALoaded() ? new CCAIntegrationImpl() : new EmptyCCAIntegration();
        CLH_INSTANCE = isCLHLoaded() ? new CLHIntegrationImpl() : new EmptyCLHIntegration();
        CMB_INSTANCE = isCMBLoaded() ? new CMBIntegrationImpl() : new EmptyCMBIntegration();
        CEE_INSTANCE = isCEELoaded() ? new CEEIntegrationImpl() : new EmptyCEEIntegration();
        CDG_INSTANCE = isCDGLoaded() ? new CDGIntegrationImpl() : new EmptyCDGIntegration();
    }
}
