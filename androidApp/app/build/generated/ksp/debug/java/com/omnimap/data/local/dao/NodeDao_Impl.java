package com.omnimap.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.omnimap.domain.model.Node;
import com.omnimap.domain.model.NodeType;
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
public final class NodeDao_Impl implements NodeDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Node> __insertionAdapterOfNode;

  private final EntityDeletionOrUpdateAdapter<Node> __deletionAdapterOfNode;

  private final EntityDeletionOrUpdateAdapter<Node> __updateAdapterOfNode;

  public NodeDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfNode = new EntityInsertionAdapter<Node>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `nodes` (`id`,`type`,`title`,`description`,`data`,`createdAt`,`updatedAt`,`x`,`y`,`isLocked`) VALUES (?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Node entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, __NodeType_enumToString(entity.getType()));
        statement.bindString(3, entity.getTitle());
        statement.bindString(4, entity.getDescription());
        statement.bindString(5, entity.getData());
        statement.bindLong(6, entity.getCreatedAt());
        statement.bindLong(7, entity.getUpdatedAt());
        statement.bindDouble(8, entity.getX());
        statement.bindDouble(9, entity.getY());
        final int _tmp = entity.isLocked() ? 1 : 0;
        statement.bindLong(10, _tmp);
      }
    };
    this.__deletionAdapterOfNode = new EntityDeletionOrUpdateAdapter<Node>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `nodes` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Node entity) {
        statement.bindString(1, entity.getId());
      }
    };
    this.__updateAdapterOfNode = new EntityDeletionOrUpdateAdapter<Node>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `nodes` SET `id` = ?,`type` = ?,`title` = ?,`description` = ?,`data` = ?,`createdAt` = ?,`updatedAt` = ?,`x` = ?,`y` = ?,`isLocked` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Node entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, __NodeType_enumToString(entity.getType()));
        statement.bindString(3, entity.getTitle());
        statement.bindString(4, entity.getDescription());
        statement.bindString(5, entity.getData());
        statement.bindLong(6, entity.getCreatedAt());
        statement.bindLong(7, entity.getUpdatedAt());
        statement.bindDouble(8, entity.getX());
        statement.bindDouble(9, entity.getY());
        final int _tmp = entity.isLocked() ? 1 : 0;
        statement.bindLong(10, _tmp);
        statement.bindString(11, entity.getId());
      }
    };
  }

  @Override
  public Object insertNode(final Node node, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfNode.insert(node);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertNodes(final List<Node> nodes, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfNode.insert(nodes);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteNode(final Node node, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfNode.handle(node);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateNode(final Node node, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfNode.handle(node);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Node>> getAllNodes() {
    final String _sql = "SELECT * FROM nodes";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"nodes"}, new Callable<List<Node>>() {
      @Override
      @NonNull
      public List<Node> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfData = CursorUtil.getColumnIndexOrThrow(_cursor, "data");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfX = CursorUtil.getColumnIndexOrThrow(_cursor, "x");
          final int _cursorIndexOfY = CursorUtil.getColumnIndexOrThrow(_cursor, "y");
          final int _cursorIndexOfIsLocked = CursorUtil.getColumnIndexOrThrow(_cursor, "isLocked");
          final List<Node> _result = new ArrayList<Node>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Node _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final NodeType _tmpType;
            _tmpType = __NodeType_stringToEnum(_cursor.getString(_cursorIndexOfType));
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpData;
            _tmpData = _cursor.getString(_cursorIndexOfData);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final float _tmpX;
            _tmpX = _cursor.getFloat(_cursorIndexOfX);
            final float _tmpY;
            _tmpY = _cursor.getFloat(_cursorIndexOfY);
            final boolean _tmpIsLocked;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsLocked);
            _tmpIsLocked = _tmp != 0;
            _item = new Node(_tmpId,_tmpType,_tmpTitle,_tmpDescription,_tmpData,_tmpCreatedAt,_tmpUpdatedAt,_tmpX,_tmpY,_tmpIsLocked);
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
  public Flow<Node> getNodeById(final String id) {
    final String _sql = "SELECT * FROM nodes WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"nodes"}, new Callable<Node>() {
      @Override
      @Nullable
      public Node call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfData = CursorUtil.getColumnIndexOrThrow(_cursor, "data");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfX = CursorUtil.getColumnIndexOrThrow(_cursor, "x");
          final int _cursorIndexOfY = CursorUtil.getColumnIndexOrThrow(_cursor, "y");
          final int _cursorIndexOfIsLocked = CursorUtil.getColumnIndexOrThrow(_cursor, "isLocked");
          final Node _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final NodeType _tmpType;
            _tmpType = __NodeType_stringToEnum(_cursor.getString(_cursorIndexOfType));
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpData;
            _tmpData = _cursor.getString(_cursorIndexOfData);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final float _tmpX;
            _tmpX = _cursor.getFloat(_cursorIndexOfX);
            final float _tmpY;
            _tmpY = _cursor.getFloat(_cursorIndexOfY);
            final boolean _tmpIsLocked;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsLocked);
            _tmpIsLocked = _tmp != 0;
            _result = new Node(_tmpId,_tmpType,_tmpTitle,_tmpDescription,_tmpData,_tmpCreatedAt,_tmpUpdatedAt,_tmpX,_tmpY,_tmpIsLocked);
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
  public Flow<List<Node>> searchNodes(final String searchQuery) {
    final String _sql = "SELECT * FROM nodes WHERE title LIKE '%' || ? || '%' OR description LIKE '%' || ? || '%'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, searchQuery);
    _argIndex = 2;
    _statement.bindString(_argIndex, searchQuery);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"nodes"}, new Callable<List<Node>>() {
      @Override
      @NonNull
      public List<Node> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfData = CursorUtil.getColumnIndexOrThrow(_cursor, "data");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfX = CursorUtil.getColumnIndexOrThrow(_cursor, "x");
          final int _cursorIndexOfY = CursorUtil.getColumnIndexOrThrow(_cursor, "y");
          final int _cursorIndexOfIsLocked = CursorUtil.getColumnIndexOrThrow(_cursor, "isLocked");
          final List<Node> _result = new ArrayList<Node>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Node _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final NodeType _tmpType;
            _tmpType = __NodeType_stringToEnum(_cursor.getString(_cursorIndexOfType));
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpData;
            _tmpData = _cursor.getString(_cursorIndexOfData);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final float _tmpX;
            _tmpX = _cursor.getFloat(_cursorIndexOfX);
            final float _tmpY;
            _tmpY = _cursor.getFloat(_cursorIndexOfY);
            final boolean _tmpIsLocked;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsLocked);
            _tmpIsLocked = _tmp != 0;
            _item = new Node(_tmpId,_tmpType,_tmpTitle,_tmpDescription,_tmpData,_tmpCreatedAt,_tmpUpdatedAt,_tmpX,_tmpY,_tmpIsLocked);
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
  public Object getAllNodesSync(final Continuation<? super List<Node>> $completion) {
    final String _sql = "SELECT * FROM nodes";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Node>>() {
      @Override
      @NonNull
      public List<Node> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfData = CursorUtil.getColumnIndexOrThrow(_cursor, "data");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfX = CursorUtil.getColumnIndexOrThrow(_cursor, "x");
          final int _cursorIndexOfY = CursorUtil.getColumnIndexOrThrow(_cursor, "y");
          final int _cursorIndexOfIsLocked = CursorUtil.getColumnIndexOrThrow(_cursor, "isLocked");
          final List<Node> _result = new ArrayList<Node>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Node _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final NodeType _tmpType;
            _tmpType = __NodeType_stringToEnum(_cursor.getString(_cursorIndexOfType));
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpData;
            _tmpData = _cursor.getString(_cursorIndexOfData);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final float _tmpX;
            _tmpX = _cursor.getFloat(_cursorIndexOfX);
            final float _tmpY;
            _tmpY = _cursor.getFloat(_cursorIndexOfY);
            final boolean _tmpIsLocked;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsLocked);
            _tmpIsLocked = _tmp != 0;
            _item = new Node(_tmpId,_tmpType,_tmpTitle,_tmpDescription,_tmpData,_tmpCreatedAt,_tmpUpdatedAt,_tmpX,_tmpY,_tmpIsLocked);
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

  private String __NodeType_enumToString(@NonNull final NodeType _value) {
    switch (_value) {
      case PROJECT: return "PROJECT";
      case TASK: return "TASK";
      case IDEA: return "IDEA";
      case GOAL: return "GOAL";
      case AGENT: return "AGENT";
      default: throw new IllegalArgumentException("Can't convert enum to string, unknown enum value: " + _value);
    }
  }

  private NodeType __NodeType_stringToEnum(@NonNull final String _value) {
    switch (_value) {
      case "PROJECT": return NodeType.PROJECT;
      case "TASK": return NodeType.TASK;
      case "IDEA": return NodeType.IDEA;
      case "GOAL": return NodeType.GOAL;
      case "AGENT": return NodeType.AGENT;
      default: throw new IllegalArgumentException("Can't convert value to enum, unknown value: " + _value);
    }
  }
}
