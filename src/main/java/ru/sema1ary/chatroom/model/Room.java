package ru.sema1ary.chatroom.model;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.*;
import ru.sema1ary.chatroom.dao.impl.RoomDaoImpl;
import ru.sema1ary.chatroom.model.user.RoomUser;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DatabaseTable(tableName = "rooms", daoClass = RoomDaoImpl.class)
public class Room {
    @DatabaseField(generatedId = true, unique = true)
    private Long id;

    @DatabaseField(canBeNull = false)
    private String name;

    @DatabaseField(canBeNull = false, columnName = "is_available")
    private boolean isAvailable;

    @DatabaseField(canBeNull = false)
    private String location;

    @ForeignCollectionField
    private ForeignCollection<RoomUser> users;
}
