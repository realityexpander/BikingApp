package com.realityexpander.runningapp.db;

import android.database.Cursor;
import android.graphics.Bitmap;
import androidx.lifecycle.LiveData;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Float;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@SuppressWarnings({"unchecked", "deprecation"})
public final class RunDao_Impl implements RunDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Run> __insertionAdapterOfRun;

  private final Converters __converters = new Converters();

  private final EntityDeletionOrUpdateAdapter<Run> __deletionAdapterOfRun;

  public RunDao_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfRun = new EntityInsertionAdapter<Run>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR REPLACE INTO `running_table` (`img`,`timestamp`,`avgSpeedInKMH`,`distanceInMeters`,`timeInMillis`,`caloriesBurned`,`id`) VALUES (?,?,?,?,?,?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, Run value) {
        final byte[] _tmp = __converters.fromBitmap(value.getImg());
        if (_tmp == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindBlob(1, _tmp);
        }
        stmt.bindLong(2, value.getTimestamp());
        stmt.bindDouble(3, value.getAvgSpeedInKMH());
        stmt.bindLong(4, value.getDistanceInMeters());
        stmt.bindLong(5, value.getTimeInMillis());
        stmt.bindLong(6, value.getCaloriesBurned());
        if (value.getId() == null) {
          stmt.bindNull(7);
        } else {
          stmt.bindLong(7, value.getId());
        }
      }
    };
    this.__deletionAdapterOfRun = new EntityDeletionOrUpdateAdapter<Run>(__db) {
      @Override
      public String createQuery() {
        return "DELETE FROM `running_table` WHERE `id` = ?";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, Run value) {
        if (value.getId() == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindLong(1, value.getId());
        }
      }
    };
  }

  @Override
  public Object insertRun(final Run run, final Continuation<? super Unit> continuation) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfRun.insert(run);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, continuation);
  }

  @Override
  public Object deleteRun(final Run run, final Continuation<? super Unit> continuation) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfRun.handle(run);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, continuation);
  }

  @Override
  public LiveData<List<Run>> getAllRunsSortedByDate() {
    final String _sql = "SELECT * FROM running_table ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[]{"running_table"}, false, new Callable<List<Run>>() {
      @Override
      public List<Run> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfImg = CursorUtil.getColumnIndexOrThrow(_cursor, "img");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfAvgSpeedInKMH = CursorUtil.getColumnIndexOrThrow(_cursor, "avgSpeedInKMH");
          final int _cursorIndexOfDistanceInMeters = CursorUtil.getColumnIndexOrThrow(_cursor, "distanceInMeters");
          final int _cursorIndexOfTimeInMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "timeInMillis");
          final int _cursorIndexOfCaloriesBurned = CursorUtil.getColumnIndexOrThrow(_cursor, "caloriesBurned");
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final List<Run> _result = new ArrayList<Run>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final Run _item;
            final Bitmap _tmpImg;
            final byte[] _tmp;
            if (_cursor.isNull(_cursorIndexOfImg)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getBlob(_cursorIndexOfImg);
            }
            _tmpImg = __converters.toBitmap(_tmp);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final float _tmpAvgSpeedInKMH;
            _tmpAvgSpeedInKMH = _cursor.getFloat(_cursorIndexOfAvgSpeedInKMH);
            final int _tmpDistanceInMeters;
            _tmpDistanceInMeters = _cursor.getInt(_cursorIndexOfDistanceInMeters);
            final long _tmpTimeInMillis;
            _tmpTimeInMillis = _cursor.getLong(_cursorIndexOfTimeInMillis);
            final int _tmpCaloriesBurned;
            _tmpCaloriesBurned = _cursor.getInt(_cursorIndexOfCaloriesBurned);
            _item = new Run(_tmpImg,_tmpTimestamp,_tmpAvgSpeedInKMH,_tmpDistanceInMeters,_tmpTimeInMillis,_tmpCaloriesBurned);
            final Integer _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getInt(_cursorIndexOfId);
            }
            _item.setId(_tmpId);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public LiveData<List<Run>> getAllRunsSortedByTimeInMillis() {
    final String _sql = "SELECT * FROM running_table ORDER BY timeInMillis DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[]{"running_table"}, false, new Callable<List<Run>>() {
      @Override
      public List<Run> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfImg = CursorUtil.getColumnIndexOrThrow(_cursor, "img");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfAvgSpeedInKMH = CursorUtil.getColumnIndexOrThrow(_cursor, "avgSpeedInKMH");
          final int _cursorIndexOfDistanceInMeters = CursorUtil.getColumnIndexOrThrow(_cursor, "distanceInMeters");
          final int _cursorIndexOfTimeInMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "timeInMillis");
          final int _cursorIndexOfCaloriesBurned = CursorUtil.getColumnIndexOrThrow(_cursor, "caloriesBurned");
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final List<Run> _result = new ArrayList<Run>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final Run _item;
            final Bitmap _tmpImg;
            final byte[] _tmp;
            if (_cursor.isNull(_cursorIndexOfImg)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getBlob(_cursorIndexOfImg);
            }
            _tmpImg = __converters.toBitmap(_tmp);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final float _tmpAvgSpeedInKMH;
            _tmpAvgSpeedInKMH = _cursor.getFloat(_cursorIndexOfAvgSpeedInKMH);
            final int _tmpDistanceInMeters;
            _tmpDistanceInMeters = _cursor.getInt(_cursorIndexOfDistanceInMeters);
            final long _tmpTimeInMillis;
            _tmpTimeInMillis = _cursor.getLong(_cursorIndexOfTimeInMillis);
            final int _tmpCaloriesBurned;
            _tmpCaloriesBurned = _cursor.getInt(_cursorIndexOfCaloriesBurned);
            _item = new Run(_tmpImg,_tmpTimestamp,_tmpAvgSpeedInKMH,_tmpDistanceInMeters,_tmpTimeInMillis,_tmpCaloriesBurned);
            final Integer _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getInt(_cursorIndexOfId);
            }
            _item.setId(_tmpId);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public LiveData<List<Run>> getAllRunsSortedByCaloriesBurned() {
    final String _sql = "SELECT * FROM running_table ORDER BY caloriesBurned DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[]{"running_table"}, false, new Callable<List<Run>>() {
      @Override
      public List<Run> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfImg = CursorUtil.getColumnIndexOrThrow(_cursor, "img");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfAvgSpeedInKMH = CursorUtil.getColumnIndexOrThrow(_cursor, "avgSpeedInKMH");
          final int _cursorIndexOfDistanceInMeters = CursorUtil.getColumnIndexOrThrow(_cursor, "distanceInMeters");
          final int _cursorIndexOfTimeInMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "timeInMillis");
          final int _cursorIndexOfCaloriesBurned = CursorUtil.getColumnIndexOrThrow(_cursor, "caloriesBurned");
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final List<Run> _result = new ArrayList<Run>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final Run _item;
            final Bitmap _tmpImg;
            final byte[] _tmp;
            if (_cursor.isNull(_cursorIndexOfImg)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getBlob(_cursorIndexOfImg);
            }
            _tmpImg = __converters.toBitmap(_tmp);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final float _tmpAvgSpeedInKMH;
            _tmpAvgSpeedInKMH = _cursor.getFloat(_cursorIndexOfAvgSpeedInKMH);
            final int _tmpDistanceInMeters;
            _tmpDistanceInMeters = _cursor.getInt(_cursorIndexOfDistanceInMeters);
            final long _tmpTimeInMillis;
            _tmpTimeInMillis = _cursor.getLong(_cursorIndexOfTimeInMillis);
            final int _tmpCaloriesBurned;
            _tmpCaloriesBurned = _cursor.getInt(_cursorIndexOfCaloriesBurned);
            _item = new Run(_tmpImg,_tmpTimestamp,_tmpAvgSpeedInKMH,_tmpDistanceInMeters,_tmpTimeInMillis,_tmpCaloriesBurned);
            final Integer _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getInt(_cursorIndexOfId);
            }
            _item.setId(_tmpId);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public LiveData<List<Run>> getAllRunsSortedByDistance() {
    final String _sql = "SELECT * FROM running_table ORDER BY distanceInMeters DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[]{"running_table"}, false, new Callable<List<Run>>() {
      @Override
      public List<Run> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfImg = CursorUtil.getColumnIndexOrThrow(_cursor, "img");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfAvgSpeedInKMH = CursorUtil.getColumnIndexOrThrow(_cursor, "avgSpeedInKMH");
          final int _cursorIndexOfDistanceInMeters = CursorUtil.getColumnIndexOrThrow(_cursor, "distanceInMeters");
          final int _cursorIndexOfTimeInMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "timeInMillis");
          final int _cursorIndexOfCaloriesBurned = CursorUtil.getColumnIndexOrThrow(_cursor, "caloriesBurned");
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final List<Run> _result = new ArrayList<Run>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final Run _item;
            final Bitmap _tmpImg;
            final byte[] _tmp;
            if (_cursor.isNull(_cursorIndexOfImg)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getBlob(_cursorIndexOfImg);
            }
            _tmpImg = __converters.toBitmap(_tmp);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final float _tmpAvgSpeedInKMH;
            _tmpAvgSpeedInKMH = _cursor.getFloat(_cursorIndexOfAvgSpeedInKMH);
            final int _tmpDistanceInMeters;
            _tmpDistanceInMeters = _cursor.getInt(_cursorIndexOfDistanceInMeters);
            final long _tmpTimeInMillis;
            _tmpTimeInMillis = _cursor.getLong(_cursorIndexOfTimeInMillis);
            final int _tmpCaloriesBurned;
            _tmpCaloriesBurned = _cursor.getInt(_cursorIndexOfCaloriesBurned);
            _item = new Run(_tmpImg,_tmpTimestamp,_tmpAvgSpeedInKMH,_tmpDistanceInMeters,_tmpTimeInMillis,_tmpCaloriesBurned);
            final Integer _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getInt(_cursorIndexOfId);
            }
            _item.setId(_tmpId);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public LiveData<List<Run>> getAllRunsSortedByAvgSpeed() {
    final String _sql = "SELECT * FROM running_table ORDER BY avgSpeedInKMH DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[]{"running_table"}, false, new Callable<List<Run>>() {
      @Override
      public List<Run> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfImg = CursorUtil.getColumnIndexOrThrow(_cursor, "img");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfAvgSpeedInKMH = CursorUtil.getColumnIndexOrThrow(_cursor, "avgSpeedInKMH");
          final int _cursorIndexOfDistanceInMeters = CursorUtil.getColumnIndexOrThrow(_cursor, "distanceInMeters");
          final int _cursorIndexOfTimeInMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "timeInMillis");
          final int _cursorIndexOfCaloriesBurned = CursorUtil.getColumnIndexOrThrow(_cursor, "caloriesBurned");
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final List<Run> _result = new ArrayList<Run>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final Run _item;
            final Bitmap _tmpImg;
            final byte[] _tmp;
            if (_cursor.isNull(_cursorIndexOfImg)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getBlob(_cursorIndexOfImg);
            }
            _tmpImg = __converters.toBitmap(_tmp);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final float _tmpAvgSpeedInKMH;
            _tmpAvgSpeedInKMH = _cursor.getFloat(_cursorIndexOfAvgSpeedInKMH);
            final int _tmpDistanceInMeters;
            _tmpDistanceInMeters = _cursor.getInt(_cursorIndexOfDistanceInMeters);
            final long _tmpTimeInMillis;
            _tmpTimeInMillis = _cursor.getLong(_cursorIndexOfTimeInMillis);
            final int _tmpCaloriesBurned;
            _tmpCaloriesBurned = _cursor.getInt(_cursorIndexOfCaloriesBurned);
            _item = new Run(_tmpImg,_tmpTimestamp,_tmpAvgSpeedInKMH,_tmpDistanceInMeters,_tmpTimeInMillis,_tmpCaloriesBurned);
            final Integer _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getInt(_cursorIndexOfId);
            }
            _item.setId(_tmpId);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public LiveData<Long> getTotalTimeInMillis() {
    final String _sql = "SELECT SUM(timeInMillis) FROM running_table";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[]{"running_table"}, false, new Callable<Long>() {
      @Override
      public Long call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Long _result;
          if(_cursor.moveToFirst()) {
            final Long _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getLong(0);
            }
            _result = _tmp;
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public LiveData<Integer> getTotalDistance() {
    final String _sql = "SELECT SUM(distanceInMeters) FROM running_table";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[]{"running_table"}, false, new Callable<Integer>() {
      @Override
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if(_cursor.moveToFirst()) {
            final Integer _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getInt(0);
            }
            _result = _tmp;
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public LiveData<Float> getTotalAvgSpeed() {
    final String _sql = "SELECT AVG(avgSpeedInKMH) FROM running_table";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[]{"running_table"}, false, new Callable<Float>() {
      @Override
      public Float call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Float _result;
          if(_cursor.moveToFirst()) {
            final Float _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getFloat(0);
            }
            _result = _tmp;
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public LiveData<Long> getTotalCaloriesBurned() {
    final String _sql = "SELECT SUM(caloriesBurned) FROM running_table";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[]{"running_table"}, false, new Callable<Long>() {
      @Override
      public Long call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Long _result;
          if(_cursor.moveToFirst()) {
            final Long _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getLong(0);
            }
            _result = _tmp;
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
