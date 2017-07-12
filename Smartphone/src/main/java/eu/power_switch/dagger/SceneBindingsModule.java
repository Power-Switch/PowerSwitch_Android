package eu.power_switch.dagger;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import eu.power_switch.gui.dialog.configuration.ConfigureSceneDialog;
import eu.power_switch.gui.fragment.configure_scene.ConfigureSceneDialogPage1Name;
import eu.power_switch.gui.fragment.configure_scene.ConfigureSceneDialogTabbedPage2Setup;

/**
 * Created by Markus on 12.07.2017.
 */
@Module
public abstract class SceneBindingsModule {

    @ContributesAndroidInjector
    abstract ConfigureSceneDialog configureSceneDialog();

    @ContributesAndroidInjector
    abstract ConfigureSceneDialogPage1Name configureSceneDialogPage1Name();

    @ContributesAndroidInjector
    abstract ConfigureSceneDialogTabbedPage2Setup configureSceneDialogTabbedPage2Setup();

}
