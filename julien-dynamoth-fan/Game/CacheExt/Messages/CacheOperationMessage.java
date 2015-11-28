package CacheExt.Messages;

import CacheExt.Operation;
import Mammoth.Util.Message.MessageImpl;

/**
 *
 * @author julien
 */
public class CacheOperationMessage extends MessageImpl {
	private static final long serialVersionUID = 1L;
	
	private String key;
	private Operation operation;
	
	public CacheOperationMessage(String key, Operation operation) {
		this.key = key;
		this.operation = operation;
	}

	public String getKey() {
		return key;
	}

	public Operation getOperation() {
		return operation;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 97 * hash + (this.operation != null ? this.operation.hashCode() : 0);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final CacheOperationMessage other = (CacheOperationMessage) obj;
		if (this.operation != other.operation && (this.operation == null || !this.operation.equals(other.operation))) {
			return false;
		}
		return true;
	}
	
	
}
