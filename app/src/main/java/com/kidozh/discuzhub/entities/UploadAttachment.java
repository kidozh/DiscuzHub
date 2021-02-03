package com.kidozh.discuzhub.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;

import static androidx.room.ForeignKey.CASCADE;

@Entity(
        foreignKeys = @ForeignKey(entity = ThreadDraft.class,
                parentColumns = "id",
                childColumns = "emp_id",
                onDelete = CASCADE),
        indices = @Index(value = {"emp_id"}, unique = true)

)
public class UploadAttachment implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    public int aid;
    public String fileName, description="";

    public int getEmpId() {
        return empId;
    }

    public void setEmpId(int empId) {
        this.empId = empId;
    }

    @ColumnInfo(name = "emp_id")
    private int empId;

    public int getId(){
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Ignore
    public UploadAttachment(int aid, String fileName) {
        this.aid = aid;
        this.fileName = fileName;
        this.description = fileName;
    }
    public UploadAttachment(int aid, String fileName, String description) {
        this.aid = aid;
        this.fileName = fileName;
        this.description = description;
    }
}
