/**
 * Copyright (C) 2011 Akiban Technologies Inc.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses.
 */

package com.akiban.server.service.servicemanager.configuration;

import com.akiban.util.ArgumentValidation;

public final class ServiceBinding {

    // DefaultServiceBinding interface

    public boolean isLocked() {
        return locked;
    }

    public void lock() {
        locked = true;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setImplementingClass(String className) {
        if (isLocked()) {
            throw new ServiceConfigurationException("can't set new implementing class: " + interfaceName + " is locked");
        }
        implementingClassName = className;
    }

    public String getImplementingClassName() {
        return implementingClassName;
    }

    public boolean isDirectlyRequired() {
        return directlyRequired;
    }

    public void markDirectlyRequired() {
        directlyRequired = true;
    }

    // Object interface

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServiceBinding)) return false;

        ServiceBinding that = (ServiceBinding) o;

        return !(interfaceName != null ? !interfaceName.equals(that.interfaceName) : that.interfaceName != null);

    }

    @Override
    public final int hashCode() {
        return interfaceName != null ? interfaceName.hashCode() : 0;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("ServiceBinding(");
        builder.append(getInterfaceName()).append(" -> ").append(getImplementingClassName());
        builder.append(
                isLocked() ? ", locked)" : ", unlocked)"
        );
        return builder.toString();
    }

    // private methods

    // DefaultLockableServiceBinding interface

    public ServiceBinding(String interfaceName) {
        ArgumentValidation.notNull("interface name", interfaceName);
        this.interfaceName = interfaceName;
    }

    // object state

    private final String interfaceName;
    private String implementingClassName;
    private boolean locked;
    private boolean directlyRequired;
}
