package org.toxsoft.skf.mnemo.gui.tsgui.layout.table;

import static org.toxsoft.core.tsgui.ved.screen.IVedScreenConstants.*;

import org.toxsoft.core.tsgui.ved.screen.*;
import org.toxsoft.core.tsgui.ved.screen.impl.*;
import org.toxsoft.core.tsgui.ved.screen.items.*;
import org.toxsoft.core.tslib.bricks.d2.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.skf.mnemo.gui.tsgui.layout.*;

/**
 * Контроллер размещения, который располагает подконтрольные элементы в виде таблицы.
 * <p>
 *
 * @author vs
 */
public class VedTableLayoutController
    implements IVedViselsLayoutController {

  /**
   * ИД типа контроллера размещения
   */
  public static final String LAYOUT_KIND = "ved.layout.table"; //$NON-NLS-1$

  // private final VedTableLayoutControllerConfig config;
  // private final IVedLayoutControllerConfig config;

  private final IVedScreen vedScreen;

  D2GridMargins margins = new D2GridMargins( 4 );

  int cellsQtty = 1;

  private final VedTableLayoutControllerConfig config;

  IStringMapEdit<CellLayoutData> cellConfigs = new StringMap<>();

  int[][] cells;

  /**
   * Конструктор.
   *
   * @param aCfg {@link IVedLayoutControllerConfig} - конфигурация контроллера
   * @param aVedScreen {@link IVedScreen} - экран редактора
   */
  public VedTableLayoutController( IVedLayoutControllerConfig aCfg, IVedScreen aVedScreen ) {
    if( aCfg != null ) {
      // TsIllegalArgumentRtException.checkFalse( aCfg.kindId().equals( LAYOUT_KIND ) );
    }
    // config = aCfg;
    config = new VedTableLayoutControllerConfig( aCfg );
    cells = new int[config.rowCount()][config.columnCount()];
    fillCels();
    printCells();
    vedScreen = aVedScreen;
  }

  // ------------------------------------------------------------------------------------
  // IVedViselsLayoutController
  //

  @Override
  public String kindId() {
    return LAYOUT_KIND;
  }

  @Override
  public void doLayout( String aMasterId, IStringList aSlaveIds ) {

    IStringListEdit idsToRemove = new StringArrayList();
    for( String slaveId : aSlaveIds ) {
      if( !cellConfigs.keys().hasElem( slaveId ) ) {
        idsToRemove.add( slaveId );
      }
    }

    for( String id : idsToRemove ) {
      cellConfigs.removeByKey( id );
      cellConfigs.put( id, new CellLayoutData() );
    }

    for( String id : aSlaveIds ) {
      if( !cellConfigs.keys().hasElem( id ) ) {
        cellConfigs.put( id, new CellLayoutData() );
      }
    }

    IStridablesList<IVedVisel> visels = VedScreenUtils.listVisels( aSlaveIds, vedScreen );

    VedAbstractVisel masterVisel = VedScreenUtils.findVisel( aMasterId, vedScreen );
    ID2Rectangle r = masterVisel.bounds();

    double currX = masterVisel.props().getDouble( PROPID_X ) + margins.left();
    double currY = masterVisel.props().getDouble( PROPID_Y ) + margins.top();

    IListEdit<Double> widthList = new ElemArrayList<>();
    double fullWidth = 0;
    for( int i = 0; i < config.columnCount(); i++ ) {
      double w = calcSingleColumnWidth( i, visels );
      fullWidth += w;
      widthList.add( Double.valueOf( w ) );
    }

    IListEdit<Double> heightList = new ElemArrayList<>();
    double fullHeight = 0;
    for( int i = 0; i < config.rowCount(); i++ ) {
      double h = calcSingleRowHeight( i, visels );
      fullHeight += h;
      heightList.add( Double.valueOf( h ) );
    }

    // ID2Margins d2m = config.margins();

    // разместим визуальные элементы
    int idx = 0;
    for( IVedVisel visel : visels ) {
      int colIdx = idx % config.columnCount();
      int rowIdx = idx / config.columnCount();
      // visel.setLocation( r.x1() + xList.get( colIdx ).doubleValue(), d2m.top() + r.y1() + rowIdx * rowHeight );
      double cellX = r.x1() + colIdx * widthList.get( colIdx ).doubleValue();
      double cellY = r.y1() + rowIdx * (heightList.get( rowIdx ).doubleValue() + config.verticalGap());
      double cellW = widthList.get( colIdx ).doubleValue();
      double cellH = heightList.get( rowIdx ).doubleValue();
      ID2Rectangle cellRect = new D2Rectangle( cellX, cellY, cellW, cellH );
      layoutCellContent( cellRect, config.columnDatas.get( colIdx ).cellData(), visel );
      idx++;
    }

  }

  @Override
  public IVedLayoutControllerConfig getConfiguration() {
    return config.config();
  }

  // ------------------------------------------------------------------------------------
  // Implementation
  //

  void fillCels() {
    int colCount = config.columnCount();
    int rowCount = config.rowCount();
    for( int i = 0; i < rowCount; i++ ) {
      for( int j = 0; j < colCount; j++ ) {
        cells[i][j] = -1;
      }
    }

    int idx = 0;
    for( CellLayoutData cld : config.cellDatas ) {
      Pair<Integer, Integer> p = findFreeCell();
      if( p != null ) {
        for( int i = 0; i < cld.verSpan(); i++ ) {
          for( int j = 0; j < cld.horSpan(); j++ ) {
            cells[p.left().intValue() + i][p.right().intValue() + j] = idx;
          }
        }
      }
      idx++;
    }
  }

  void printCells() {
    int colCount = config.columnCount();
    int rowCount = config.rowCount();
    for( int i = 0; i < rowCount; i++ ) {
      for( int j = 0; j < colCount; j++ ) {
        System.out.print( String.format( "%02d, ", Integer.valueOf( cells[i][j] ) ) ); //$NON-NLS-1$
      }
      System.out.println();
    }
  }

  Pair<Integer, Integer> findFreeCell() {
    int colCount = config.columnCount();
    int rowCount = config.rowCount();
    for( int i = 0; i < rowCount; i++ ) {
      for( int j = 0; j < colCount; j++ ) {
        if( cells[i][j] == -1 ) {
          return new Pair<>( Integer.valueOf( i ), Integer.valueOf( j ) );
        }
      }
    }
    return null;
  }

  double calcCellWidth( IVedVisel aVisel, CellLayoutData aCellData ) {
    double mw = aCellData.margins().left() + aCellData.margins().right();
    return mw + aVisel.bounds().width() / aCellData.horSpan();
  }

  double calcCellHeight( IVedVisel aVisel, CellLayoutData aCellData ) {
    ID2Rectangle r = aVisel.bounds();
    return r.height() / aCellData.verSpan() + aCellData.margins().top() + aCellData.margins().bottom();
  }

  double calcSingleRowHeight( int aRow, IStridablesList<IVedVisel> aVisels ) {
    IStridablesListEdit<IVedVisel> visels = new StridablesList<>();
    for( int i = 0; i < config.columnCount(); i++ ) {
      int idx = cells[aRow][i];
      if( idx != -1 && idx < aVisels.size() ) {
        visels.add( aVisels.get( idx ) );
      }
    }

    double rowHeight = 0.;
    for( int i = 0; i < aVisels.size(); i++ ) {
      IVedVisel visel = aVisels.get( i );
      CellLayoutData cld = config.cellDatas().get( i );
      double height = calcCellHeight( visel, cld );
      if( height > rowHeight ) {
        rowHeight = height;
      }
      i++;
    }
    return rowHeight;
  }

  double calcSingleColumnWidth( int aColumn, IStridablesList<IVedVisel> aVisels ) {
    // TableColumnLayoutData cld = config.columnDatas().get( aColumnIdx );
    // double minWidth = cld.widthRange().left().doubleValue();
    // double maxWidth = cld.widthRange().right().doubleValue();
    // if( (minWidth >= 0) && (maxWidth >= 0) && (Double.compare( minWidth, maxWidth ) == 0) ) {
    // return minWidth;
    // }

    IStridablesListEdit<IVedVisel> visels = new StridablesList<>();
    for( int i = 0; i < config.rowCount(); i++ ) {
      int idx = cells[i][aColumn];
      if( idx != -1 && idx < aVisels.size() ) {
        visels.add( aVisels.get( idx ) );
      }
    }

    double columnWidth = 0.;
    for( int i = 0; i < visels.size(); i++ ) {
      IVedVisel visel = visels.get( i );
      CellLayoutData cld = config.cellDatas().get( i );
      double width = calcCellWidth( visel, cld );
      if( width > columnWidth ) {
        columnWidth = width;
      }
    }

    // if( minWidth >= 0 && columnWidth < minWidth ) {
    // return minWidth;
    // }
    //
    // if( maxWidth >= 0 && columnWidth > maxWidth ) {
    // return maxWidth;
    // }

    return columnWidth;
  }

  @Override
  public IList<ID2Rectangle> calcCellRects( String aMasterId, IStringList aSlaveIds ) {
    // TODO Auto-generated method stub
    return null;
  }

  void layoutCellContent( ID2Rectangle aCellBounds, CellLayoutData aLayoutData, IVedVisel aVisel ) {
    ID2Rectangle clientRect = calcClientRect( aCellBounds, aLayoutData.margins() );
    if( aLayoutData.fillCellWidth() ) {
      aVisel.setSize( clientRect.width(), aVisel.height() );
    }
    if( aLayoutData.fillCellHeight() ) {
      aVisel.setSize( aVisel.width(), clientRect.height() );
    }

    double viselX;
    double viselY;

    viselX = switch( aLayoutData.cellAlignment().horAlignment() ) {
      case CENTER -> clientRect.x1() + (clientRect.width() - aVisel.bounds().width()) / 2.;
      case LEFT -> clientRect.x1();
      case RIGHT -> clientRect.x1() + clientRect.width() - aVisel.bounds().width();
      case FILL -> throw new IllegalArgumentException(
          "Unexpected value: " + aLayoutData.cellAlignment().horAlignment() ); //$NON-NLS-1$
    };

    viselY = switch( aLayoutData.cellAlignment().verAlignment() ) {
      case CENTER -> clientRect.y1() + (clientRect.height() - aVisel.bounds().height()) / 2.;
      case TOP -> clientRect.y1();
      case BOTTOM -> clientRect.y1() + clientRect.height() - aVisel.bounds().height();
      case FILL -> throw new IllegalArgumentException(
          "Unexpected value: " + aLayoutData.cellAlignment().horAlignment() ); //$NON-NLS-1$
    };
    aVisel.setLocation( viselX, viselY );
  }

  ID2Rectangle calcClientRect( ID2Rectangle aBounds, ID2Margins aMargins ) {
    double x = aBounds.x1() + aMargins.left();
    double y = aBounds.y1() + aMargins.top();
    double width = aBounds.width() - aMargins.left() - aMargins.right();
    if( width < 1 ) {
      width = 1;
    }
    double height = aBounds.height() - aMargins.top() - aMargins.bottom();
    if( height < 1 ) {
      height = 1;
    }
    return new D2Rectangle( x, y, width, height );
  }

}
