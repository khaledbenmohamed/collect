/**
 * Generated by Gas3 v2.3.0 (Granite Data Services).
 *
 * WARNING: DO NOT CHANGE THIS FILE. IT MAY BE OVERWRITTEN EACH TIME YOU USE
 * THE GENERATOR.
 */

package org.openforis.collect.metamodel.proxy {

    import org.granite.util.Enum;

    [Bindable]
    [RemoteClass(alias="org.openforis.collect.metamodel.proxy.NodeLabelProxy$Type")]
    public class NodeLabelProxy$Type extends Enum {

        public static const ABBREVIATED:NodeLabelProxy$Type = new NodeLabelProxy$Type("ABBREVIATED", _);
        public static const HEADING:NodeLabelProxy$Type = new NodeLabelProxy$Type("HEADING", _);
        public static const INSTANCE:NodeLabelProxy$Type = new NodeLabelProxy$Type("INSTANCE", _);
        public static const NUMBER:NodeLabelProxy$Type = new NodeLabelProxy$Type("NUMBER", _);
        public static const REPORTING:NodeLabelProxy$Type = new NodeLabelProxy$Type("REPORTING", _);

        function NodeLabelProxy$Type(value:String = null, restrictor:* = null) {
            super((value || ABBREVIATED.name), restrictor);
        }

        protected override function getConstants():Array {
            return constants;
        }

        public static function get constants():Array {
            return [ABBREVIATED, HEADING, INSTANCE, NUMBER, REPORTING];
        }

        public static function valueOf(name:String):NodeLabelProxy$Type {
            return NodeLabelProxy$Type(ABBREVIATED.constantOf(name));
        }
    }
}