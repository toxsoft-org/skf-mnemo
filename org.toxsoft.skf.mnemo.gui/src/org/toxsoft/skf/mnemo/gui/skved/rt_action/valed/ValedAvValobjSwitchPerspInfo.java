package org.toxsoft.skf.mnemo.gui.skved.rt_action.valed;

import static org.toxsoft.core.tsgui.valed.api.IValedControlConstants.*;

import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.valed.api.*;
import org.toxsoft.core.tsgui.valed.controls.av.*;
import org.toxsoft.core.tsgui.valed.impl.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.skf.mnemo.gui.skved.rt_action.*;

/**
 * {@link EAtomicType#VALOBJ} of type {@link SwitchPerspInfo} editor.
 * <p>
 * Wraps over {@link ValedPopupMnemoInfo}.
 *
 * @author dima
 */
public class ValedAvValobjSwitchPerspInfo
    extends AbstractAvValobjWrapperValedControl<SwitchPerspInfo> {

  /**
   * The factory name.
   */
  public static final String FACTORY_NAME = VALED_EDNAME_PREFIX + ".ValedAvValobjSwitchPerspInfo"; //$NON-NLS-1$

  /**
   * The factory class.
   *
   * @author dima
   */
  static class Factory
      extends AbstractValedControlFactory {

    protected Factory() {
      super( FACTORY_NAME );
    }

    @SuppressWarnings( "unchecked" )
    @Override
    protected IValedControl<IAtomicValue> doCreateEditor( ITsGuiContext aContext ) {
      return new ValedAvValobjSwitchPerspInfo( aContext );
    }

    @Override
    protected boolean isSuitableAvEditor( EAtomicType aAtomicType, String aKeeperId, ITsGuiContext aEditorContext ) {
      if( aAtomicType == EAtomicType.VALOBJ && aKeeperId != null ) {
        return aKeeperId.equals( SwitchPerspInfo.KEEPER_ID );
      }
      return false;
    }

  }

  /**
   * The factory singleton.
   */
  public static final AbstractValedControlFactory FACTORY = new Factory();

  protected ValedAvValobjSwitchPerspInfo( ITsGuiContext aTsContext ) {
    super( aTsContext, ValedSwitchPerspInfo.FACTORY );
  }

}
