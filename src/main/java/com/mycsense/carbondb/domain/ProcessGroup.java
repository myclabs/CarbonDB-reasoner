package com.mycsense.carbondb.domain;

import java.util.HashSet;

public class ProcessGroup extends Group {
    protected HashSet<Process> processes;

    public HashSet<Process> getProcesses() {
        return processes;
    }

    public void setProcesses(HashSet<Process> processes) {
        this.processes = processes;
    }

    public void addProcess(Process process) {
        processes.add(process);
    }
}
