package com.omnimap.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.omnimap.domain.model.Edge;
import com.omnimap.domain.model.EdgeType;
import java.lang.Class;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class EdgeDao_Impl implements EdgeDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Edge> __insertionAdapterOfEdge;

  private final EntityDeletionOrUpdateAdapter<Edge> __deletionAdapterOfEdge;

  private final EntityDeletionOrUpdateAdapter<Edge> __updateAdapterOfEdge;

  public EdgeDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfEdge = new EntityInsertionAdapter<Edge>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `edges` (`id`,`sourceId`,`targetId`,`type`,`createdAt`) VALUES (?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Edge entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getSourceId());
        statement.bindString(3, entity.getTargetId());
        statement.bindString(4, __EdgeType_enumToString(entity.getType()));
        statement.bindLong(5, entity.getCreatedAt());
      }
    };
    this.__deletionAdapterOfEdge = new EntityDeletionOrUpdateAdapter<Edge>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `edges` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Edge entity) {
        statement.bindString(1, entity.getId());
      }
    };
    this.__updateAdapterOfEdge = new EntityDeletionOrUpdateAdapter<Edge>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `edges` SET `id` = ?,`sourceId` = ?,`targetId` = ?,`type` = ?,`createdAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Edge entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getSourceId());
        statement.bindString(3, entity.getTargetId());
        statement.bindString(4, __EdgeType_enumToString(entity.getType()));
        statement.bindLong(5, entity.getCreatedAt());
        statement.bindString(6, entity.getId());
      }
    };
  }

  @Override
  public Object insertEdge(final Edge edge, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfEdge.insert(edge);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertEdges(final List<Edge> edges, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfEdge.insert(edges);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteEdge(final Edge edge, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfEdge.handle(edge);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateEdge(final Edge edge, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfEdge.handle(edge);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Edge>> getAllEdges() {
    final String _sql = "SELECT * FROM edges";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"edges"}, new Callable<List<Edge>>() {
      @Override
      @NonNull
      public List<Edge> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSourceId = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceId");
          final int _cursorIndexOfTargetId = CursorUtil.getColumnIndexOrThrow(_cursor, "targetId");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<Edge> _result = new ArrayList<Edge>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Edge _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpSourceId;
            _tmpSourceId = _cursor.getString(_cursorIndexOfSourceId);
            final String _tmpTargetId;
            _tmpTargetId = _cursor.getString(_cursorIndexOfTargetId);
            final EdgeType _tmpType;
            _tmpType = __EdgeType_stringToEnum(_cursor.getString(_cursorIndexOfType));
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new Edge(_tmpId,_tmpSourceId,_tmpTargetId,_tmpType,_tmpCreatedAt);
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
  public Flow<List<Edge>> getEdgesForNode(final String nodeId) {
    final String _sql = "SELECT * FROM edges WHERE sourceId = ? OR targetId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, nodeId);
    _argIndex = 2;
    _statement.bindString(_argIndex, nodeId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"edges"}, new Callable<List<Edge>>() {
      @Override
      @NonNull
      public List<Edge> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSourceId = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceId");
          final int _cursorIndexOfTargetId = CursorUtil.getColumnIndexOrThrow(_cursor, "targetId");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<Edge> _result = new ArrayList<Edge>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Edge _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpSourceId;
            _tmpSourceId = _cursor.getString(_cursorIndexOfSourceId);
            final String _tmpTargetId;
            _tmpTargetId = _cursor.getString(_cursorIndexOfTargetId);
            final EdgeType _tmpType;
            _tmpType = __EdgeType_stringToEnum(_cursor.getString(_cursorIndexOfType));
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new Edge(_tmpId,_tmpSourceId,_tmpTargetId,_tmpType,_tmpCreatedAt);
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
  public Object getAllEdgesSync(final Continuation<? super List<Edge>> $completion) {
    final String _sql = "SELECT * FROM edges";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Edge>>() {
      @Override
      @NonNull
      public List<Edge> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSourceId = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceId");
          final int _cursorIndexOfTargetId = CursorUtil.getColumnIndexOrThrow(_cursor, "targetId");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<Edge> _result = new ArrayList<Edge>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Edge _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpSourceId;
            _tmpSourceId = _cursor.getString(_cursorIndexOfSourceId);
            final String _tmpTargetId;
            _tmpTargetId = _cursor.getString(_cursorIndexOfTargetId);
            final EdgeType _tmpType;
            _tmpType = __EdgeType_stringToEnum(_cursor.getString(_cursorIndexOfType));
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new Edge(_tmpId,_tmpSourceId,_tmpTargetId,_tmpType,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }

  private String __EdgeType_enumToString(@NonNull final EdgeType _value) {
    switch (_value) {
      case PARENT_OF: return "PARENT_OF";
      case DEPENDS_ON: return "DEPENDS_ON";
      case RELATED_TO: return "RELATED_TO";
      default: throw new IllegalArgumentException("Can't convert enum to string, unknown enum value: " + _value);
    }
  }

  private EdgeType __EdgeType_stringToEnum(@NonNull final String _value) {
    switch (_value) {
      case "PARENT_OF": return EdgeType.PARENT_OF;
      case "DEPENDS_ON": return EdgeType.DEPENDS_ON;
      case "RELATED_TO": return EdgeType.RELATED_TO;
      default: throw new IllegalArgumentException("Can't convert value to enum, unknown value: " + _value);
    }
  }
}
